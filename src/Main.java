import mpi.MPI;
import util.DataSplitter;
import util.MathUtils;

import java.util.Arrays;

/*
 * Курсова робота з дисципліни «Архітектура комп'ютерів»
 * Тема: Розробка програмного забезпечення для комп'ютерів за багатоядерною архітектурою
 * Розділ 3. ПРГ2
 * Математичний вираз: a = min(C*MZ) + max(D*(MX*MR))
 *
 * Варіант введення-виведення:
 * Т1 вводить C, MX
 * Тр вводить MR, D, MZ
 * Т1 обчислює a та виводить результат
 *
 * Студент: Лісовський Н. Р.
 * Група: ІО-33
 * Мова програмування: Java
 */
public class Main {
    private static final int TAG_INPUT_DATA = 100;
    private static final int TAG_RESULT = 200;
    private static final int N = 1200; // Розмірність матриць та векторів

    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int P = MPI.COMM_WORLD.Size();
        int H = N / P;

        if (P < 3) {
            if (rank == 0) {
                System.out.println("Помилка: Для топології 'Зірка' потрібно мінімум 3 потоки!");
            }
            MPI.Finalize();
            return;
        }

        // Локальні масиви
        long[] C = new long[N];
        long[] MR = new long[N * N];
        long[] MXh = new long[H * N];
        long[] MZh = new long[N * H];
        long[] Dh = new long[H];

        long start = System.currentTimeMillis();

        // Введення та розсилка вхідних даних
        if (rank == 0) { // Потік Т1
            // Введення вектору C та матриці MX
            C = fillVector(1L, N);
            long[] MX = fillMatrix(1L, N);
            System.arraycopy(MX, 0, MXh, 0, H * N);

            // Передати потоку Т2 дані C, MX
            MPI.COMM_WORLD.Send(C, 0, N, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Send(MX, 0, N * N, MPI.LONG, 1, TAG_INPUT_DATA);

            // Отримати від потоку Т2 дані MR, MZh, Dh
            MPI.COMM_WORLD.Recv(MR, 0, N * N, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(MZh, 0, N * H, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(Dh, 0, H, MPI.LONG, 1, TAG_INPUT_DATA);

        } else if (rank == P - 1) { // Потік ТР
            // Введення вектору D та матриць MR, MZ
            MR = fillMatrix(1L, N);
            long[] MZ = fillMatrix(1L, N);
            long[] D = fillVector(1L, N);

            MZh = DataSplitter.getColumnBlock(MZ, P - 1, H, N);
            Dh = DataSplitter.getVectorBlock(D, P - 1, H);

            // Передати потоку Т2 дані MR, MZ, D
            MPI.COMM_WORLD.Send(MR, 0, N * N, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Send(MZ, 0, N * N, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Send(D, 0, N, MPI.LONG, 1, TAG_INPUT_DATA);

            // Отримати від потоку Т2 дані C, MXh
            MPI.COMM_WORLD.Recv(C, 0, N, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(MXh, 0, H * N, MPI.LONG, 1, TAG_INPUT_DATA);

        } else if (rank == 1) { // Потік Т2
            // Отримати від потоку Т1 дані C, MX
            long[] MX = new long[N * N];
            MPI.COMM_WORLD.Recv(C, 0, N, MPI.LONG, 0, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(MX, 0, N * N, MPI.LONG, 0, TAG_INPUT_DATA);

            // Отримати від потоку ТР дані MR, MZ, D
            long[] MZ = new long[N * N];
            long[] D = new long[N];
            MPI.COMM_WORLD.Recv(MR, 0, N * N, MPI.LONG, P - 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(MZ, 0, N * N, MPI.LONG, P - 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(D, 0, N, MPI.LONG, P - 1, TAG_INPUT_DATA);

            // Передати потоку T1 дані MR, MZh, Dh
            MPI.COMM_WORLD.Send(MR, 0, N * N, MPI.LONG, 0, TAG_INPUT_DATA);
            long[] MZh_0 = DataSplitter.getColumnBlock(MZ, 0, H, N);
            MPI.COMM_WORLD.Send(MZh_0, 0, N * H, MPI.LONG, 0, TAG_INPUT_DATA);
            long[] Dh_0 = DataSplitter.getVectorBlock(D, 0, H);
            MPI.COMM_WORLD.Send(Dh_0, 0, H, MPI.LONG, 0, TAG_INPUT_DATA);

            // Передати потоку ТР дані C, MXh
            MPI.COMM_WORLD.Send(C, 0, N, MPI.LONG, P - 1, TAG_INPUT_DATA);
            long[] MXh_P = DataSplitter.getRowBlock(MX, P - 1, H, N);
            MPI.COMM_WORLD.Send(MXh_P, 0, H * N, MPI.LONG, P - 1, TAG_INPUT_DATA);

            MXh = DataSplitter.getRowBlock(MX, 1, H, N);
            MZh = DataSplitter.getColumnBlock(MZ, 1, H, N);
            Dh = DataSplitter.getVectorBlock(D, 1, H);

            // Передати потокам Тi дані C, MR, MXh, MZh, Dh
            for (int i = 2; i < P - 1; i++) {
                MPI.COMM_WORLD.Send(C, 0, N, MPI.LONG, i, TAG_INPUT_DATA);
                MPI.COMM_WORLD.Send(MR, 0, N * N, MPI.LONG, i, TAG_INPUT_DATA);
                long[] MXh_i = DataSplitter.getRowBlock(MX, i, H, N);
                MPI.COMM_WORLD.Send(MXh_i, 0, H * N, MPI.LONG, i, TAG_INPUT_DATA);
                long[] MZh_i = DataSplitter.getColumnBlock(MZ, i, H, N);
                MPI.COMM_WORLD.Send(MZh_i, 0, N * H, MPI.LONG, i, TAG_INPUT_DATA);
                long[] Dh_i = DataSplitter.getVectorBlock(D, i, H);
                MPI.COMM_WORLD.Send(Dh_i, 0, H, MPI.LONG, i, TAG_INPUT_DATA);
            }

        } else { // Потоки Тi
            // Отримати від потоку Т2 дані C, MR, MXh, MZh, Dh
            MPI.COMM_WORLD.Recv(C, 0, N, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(MR, 0, N * N, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(MXh, 0, H * N, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(MZh, 0, N * H, MPI.LONG, 1, TAG_INPUT_DATA);
            MPI.COMM_WORLD.Recv(Dh, 0, H, MPI.LONG, 1, TAG_INPUT_DATA);
        }

        // Обчислити Zh=C*MZh
        long[] Zh = MathUtils.multiplyVectorByMatrix(C, MZh, N, H);

        // Обчислити m1i=min(Zh)
        long m1i = MathUtils.findMin(Zh);

        // Обчислити MQh=MXh*MR
        long[] MQh = MathUtils.multiplyMatrixBlocks(MXh, MR, H, N);

        // Обчислити Ei=Dh*MQh
        long[] Ei = MathUtils.multiplyVectorByMatrix(Dh, MQh, H, N);


        // Збір результатів та редукція
        if (rank != 0 && rank != 1) { // Потоки Т3-Р
            // Передати потоку Т2 дані m1i, Ei
            long[] localM1 = new long[]{m1i};
            MPI.COMM_WORLD.Send(localM1, 0, 1, MPI.LONG, 1, TAG_RESULT);
            MPI.COMM_WORLD.Send(Ei, 0, N, MPI.LONG, 1, TAG_RESULT);

        } else if (rank == 1) { // Потік Т2
            // Отримати від потоків Тi-P дані m1(i-P), E(i-P)
            long[] allM1 = new long[P];
            long[] allE = new long[P * N];

            allM1[1] = m1i;
            System.arraycopy(Ei, 0, allE, N, N);

            for (int i = 2; i < P; i++) {
                long[] tempM1 = new long[1];
                MPI.COMM_WORLD.Recv(tempM1, 0, 1, MPI.LONG, i, TAG_RESULT);
                allM1[i] = tempM1[0];

                long[] tempE = new long[N];
                MPI.COMM_WORLD.Recv(tempE, 0, N, MPI.LONG, i, TAG_RESULT);
                System.arraycopy(tempE, 0, allE, i * N, N);
            }

            // Передати потоку Т1 дані m1(2-P), E(2-P)
            MPI.COMM_WORLD.Send(allM1, 0, P, MPI.LONG, 0, TAG_RESULT);
            MPI.COMM_WORLD.Send(allE, 0, P * N, MPI.LONG, 0, TAG_RESULT);

        } else { // Потік Т1
            // Отримати від потоку Т2 дані m1(2-P), E(2-P)
            long[] allM1 = new long[P];
            long[] allE = new long[P * N];

            MPI.COMM_WORLD.Recv(allM1, 0, P, MPI.LONG, 1, TAG_RESULT);
            MPI.COMM_WORLD.Recv(allE, 0, P * N, MPI.LONG, 1, TAG_RESULT);

            // Обчислити m1=min(m11,m12,...,m1P)
            long m1 = MathUtils.findGlobalMin(m1i, allM1);

            // Обчислити E=E1+E2+...+EP
            long[] E = MathUtils.sumVectors(Ei, allE, P, N);

            // Обчислити m2=max(E)
            long m2 = MathUtils.findMax(E);

            // Обчислити a=m1+m2
            long a = m1 + m2;

            long end = System.currentTimeMillis();

            System.out.println("Final result a = " + a);
            System.out.println("Time: " + (end - start) + " ms");
        }

        MPI.Finalize();
    }

    private static long[] fillVector(long value, int size) {
        long[] vector = new long[size];
        Arrays.fill(vector, value);
        return vector;
    }

    private static long[] fillMatrix(long value, int size) {
        long[] matrix = new long[size * size];
        Arrays.fill(matrix, value);
        return matrix;
    }
}