import mpi.MPI;
import util.DataSplitter;
import util.MathUtils;

import java.util.Arrays;

public class Main {
    private static final int TAG_INPUT_DATA = 100;
    private static final int TAG_RESULT = 200;

    private static final int N = 1200;

    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int P = MPI.COMM_WORLD.Size();
        int H = N / P;

        if (P < 3) {
            if (rank == 0) {
                System.out.println("The program requires at least 3 threads.");
            }
            MPI.Finalize();
            return;
        }

        // Оголошення локальних змінних
        long[] C;
        long[][] MR;
        long[][] MXh;
        long[][] MZh;
        long[] Dh;

        // Введення та розсилка вхідних даних
        if (rank == 0) { // Потік Т1
            // Введення вектору C та матриці MX
            C = fillVector(1L);
            long[][] MX = fillMatrix(1L);

            MXh = DataSplitter.getMatrixRowBlock(MX, 0, H);

            // Передати потоку Т2 дані C, MX(P-1)h
            MPI.COMM_WORLD.Send(new Object[]{C, MX}, 0, 2, MPI.OBJECT, 1, TAG_INPUT_DATA);

            // Отримати від потоку Т2 дані MR, MZh, Dh
            Object[] recv = new Object[3];
            MPI.COMM_WORLD.Recv(recv, 0, recv.length, MPI.OBJECT, 1, TAG_INPUT_DATA);
            MR = (long[][]) recv[0];
            MZh = DataSplitter.getMatrixColumnBlock((long[][]) recv[1], 0, H);
            Dh = DataSplitter.getVectorBlock((long[]) recv[2], 0, H);

        } else if (rank == P - 1) { // Потік ТP
            // Введення вектору D та матриць MR, MZ
            MR = fillMatrix(1L);
            long[][] MZ = fillMatrix(1L);
            long[] D = fillVector(1L);

            MZh = DataSplitter.getMatrixColumnBlock(MZ, P - 1, H);
            Dh = DataSplitter.getVectorBlock(D, P - 1, H);

            // Передати потоку Т2 дані MR, MZ(P-1)h, D(P-1)h
            MPI.COMM_WORLD.Send(new Object[]{MR, MZ, D}, 0, 3, MPI.OBJECT, 1, TAG_INPUT_DATA);

            // Отримати від потоку Т2 дані C, MXh
            Object[] recv = new Object[2];
            MPI.COMM_WORLD.Recv(recv, 0, recv.length, MPI.OBJECT, 1, TAG_INPUT_DATA);
            C = (long[]) recv[0];
            MXh = DataSplitter.getMatrixRowBlock((long[][]) recv[1], rank, H);

        } else if (rank == 1) { // Потік Т2
            // Отримати від потоку Т1 дані C, MX(P-1)h
            Object[] recv = new Object[2];
            MPI.COMM_WORLD.Recv(recv, 0, recv.length, MPI.OBJECT, 0, TAG_INPUT_DATA);
            C = (long[]) recv[0];
            long[][] MX = (long[][]) recv[1];

            MXh = DataSplitter.getMatrixRowBlock(MX, rank, H);

            // Отримати від потоку ТР дані MR, MZ(P-1)h, D(P-1)h
            recv = new Object[3];
            MPI.COMM_WORLD.Recv(recv, 0, recv.length, MPI.OBJECT, P - 1, TAG_INPUT_DATA);
            MR = (long[][]) recv[0];
            long[][] MZ = (long[][]) recv[1];
            long[] D = (long[]) recv[2];

            MZh = DataSplitter.getMatrixColumnBlock(MZ, rank, H);
            Dh = DataSplitter.getVectorBlock(D, rank, H);

            // Передати потоку T1 дані MR, MZh, Dh
            MPI.COMM_WORLD.Send(new Object[]{MR, MZ, D}, 0, 3, MPI.OBJECT, 0, TAG_INPUT_DATA);

            // Передати потоку ТР дані C, MXh
            MPI.COMM_WORLD.Send(new Object[]{C, MX}, 0, 2, MPI.OBJECT, P - 1, TAG_INPUT_DATA);

            // Передати потокам Тi дані C, MR, MXh, MZh, Dh
            Object[] sendBuf = new Object[]{C, MR, MX, MZ, D};
            for (int i = 2; i < P - 1; i++) {
                MPI.COMM_WORLD.Send(sendBuf, 0, sendBuf.length, MPI.OBJECT, i, TAG_INPUT_DATA);
            }

        } else { // Потоки Ті
            // Отримати від потоку Т2 дані C, MR, MXh, MZh, Dh
            Object[] recv = new Object[5];
            MPI.COMM_WORLD.Recv(recv, 0, recv.length, MPI.OBJECT, 1, TAG_INPUT_DATA);
            C = (long[]) recv[0];
            MR = (long[][]) recv[1];
            MXh = DataSplitter.getMatrixRowBlock((long[][]) recv[2], rank, H);
            MZh = DataSplitter.getMatrixColumnBlock((long[][]) recv[3], rank, H);
            Dh = DataSplitter.getVectorBlock((long[]) recv[4], rank, H);
        }

        // Обчислити Zh=C*MZh
        long[] Zh = MathUtils.multiplyVectorByMatrix(C, MZh);

        // Обчислити m1i=min(Zh)
        long m1i = MathUtils.findMin(Zh);

        // Обчислити MQh=MXh*MR
        long[][] MQh = MathUtils.multiplyMatrixBlocks(MXh, MR);

        // Обчислити Ei=Dh*MQh
        long[] Ei = MathUtils.multiplyVectorByMatrix(Dh, MQh);

        // Збір результатів та редукція
        if (rank != 0 && rank != 1) { // Потоки Т3-Р
            // Передати потоку Т2 дані m1i, Ei
            MPI.COMM_WORLD.Send(new Object[]{m1i, Ei}, 0, 2, MPI.OBJECT, 1, TAG_RESULT);

        } else if (rank == 1) { // Потік Т2
            // Отримати від потоків Тi-P дані m1(i-P), E(i-P)
            long[] allM1 = new long[P];
            long[][] allE = new long[P][N];
            Arrays.fill(allM1, Long.MAX_VALUE);
            allM1[1] = m1i;
            allE[1] = Ei;

            for (int i = 2; i < P; i++) {
                Object[] recv = new Object[2];
                MPI.COMM_WORLD.Recv(recv, 0, recv.length, MPI.OBJECT, i, TAG_RESULT);
                allM1[i] = (long) recv[0];
                allE[i] = (long[]) recv[1];
            }

            // Передати потоку Т1 дані m1(2-P), E(2-P)
            MPI.COMM_WORLD.Send(new Object[]{allM1, allE}, 0, 2, MPI.OBJECT, 0, TAG_RESULT);

        } else { // Потік Т1
            // Отримати від потоку Т2 дані m1(2-P), E(2-P)
            Object[] recv = new Object[2];
            MPI.COMM_WORLD.Recv(recv, 0, recv.length, MPI.OBJECT, 1, TAG_RESULT);
            long[] allM1 = (long[]) recv[0];
            long[][] allE = (long[][]) recv[1];

            // Обчислити m1=min(m11,m12,...,m1P)
            long m1 = MathUtils.findGlobalMin(m1i, allM1);

            // Обчислити E=E1+E2+...+EP
            long[] E = MathUtils.sumVectors(Ei, allE);

            // Обчислити m2=max(E)
            long m2 = MathUtils.findMax(E);

            // Обчислити a=m1+m2
            long a = m1 + m2;

            // Виведення a
            System.out.println("Final result a = " + a);
        }

        MPI.Finalize();
    }

    private static long[] fillVector(long value) {
        long[] vector = new long[N];
        Arrays.fill(vector, value);
        return vector;
    }

    private static long[][] fillMatrix(long value) {
        long[][] matrix = new long[N][N];
        for (long[] row : matrix) {
            Arrays.fill(row, value);
        }
        return matrix;
    }
}