import java.util.Arrays;

public class ComputeNode extends Thread {
    private int rank;
    private int P;
    private int N;
    private StarNetwork network;

    public ComputeNode(int rank, int P, int N, StarNetwork network) {
        this.rank = rank;
        this.P = P;
        this.N = N;
        this.network = network;
    }

    @Override
    public void run() {
        try {
            switch (rank) {
                case 1:
                    runT1();
                    break;
                case 2:
                    runT2();
                    break;
                default:
                    if (rank == P) {
                        runTP();
                    } else {
                        runTi();
                    }
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runT1() {
        // Введення вектору C та матриці MX
        long[] C = new long[N];
        fillVector(C, 1L);
        long[][] MX = new long[N][N];
        fillMatrix(MX, 1L);

        // Передати потоку Т2 дані C, MX(P-1)h
        network.send(new Message(rank, 2, C, MX));

        // Отримати від потоку Т2 дані MR, MZh, Dh
        Message message = network.receive(rank);
        long[][] MR = (long[][])  message.payload[0];
        long[][] MZh = (long[][])  message.payload[1];
        long[] Dh = (long[])  message.payload[2];

        // Обчислити Zh=C*MZh


        // Обчислити m11=min(Zh)
        // Обчислити MQh=MXh*MR
        // Обчислити E1=Dh*MQh
        // Отримати від потоку Т2 дані m1(2-P), E(2-P)
        // Обчислити m1=min⁡(m11,m12,…,m1P)
        // Обчислити E=E1+E2+⋯+EP
        // Обчислити m2=max⁡(E)
        // Обчислити a=m1+m2
        // Виведення a
    }

    private void runT2() {
        // Отримати від потоку Т1 дані C, MX(P-1)h
        // Отримати від потоку ТР дані MR, MZ(P-1)h, D(P-1)h
        // Передати потоку T1 дані MR, MZh, Dh
        // Передати потоку ТР дані C, MXh
        // Передати потокам Тi дані C, MR, MXh, MZh, Dh
        // Обчислити Zh=C*MZh
        // Обчислити m12=min(Zh)
        // Обчислити MQh=MXh*MR
        // Обчислити E2=Dh*MQh
        // Отримати від потоків Тi-P дані m1(i-P), E(i-P)
        // Передати потоку Т1 дані m1(2-P), E(2-P)
    }

    private void runTi() {
        // Отримати від потоку Т2 дані C, MR, MXh, MZh, Dh
        // Обчислити Zh=C*MZh
        // Обчислити m1i=min(Zh)
        // Обчислити MQh=MXh*MR
        // Обчислити Ei=Dh*MQh
        // Передати потоку Т2 дані m1i, Ei
    }

    private void runTP() {
        // Введення вектору D та матриць MR, MZ
        // Передати потоку Т2 дані MR, MZ(P-1)h, D(P-1)h
        // Отримати від потоку Т2 дані C, MXh
        // Обчислити Zh=C*MZh
        // Обчислити m1P=min(Zh)
        // Обчислити MQh=MXh*MR
        // Обчислити EP=Dh*MQh
        // Передати потоку Т2 дані m1P, EP
    }

    private void fillVector(long[] vector, long value) {
        Arrays.fill(vector, value);
    }

    private void fillMatrix(long[][] matrix, long value) {
        for (long[] row : matrix) {
            Arrays.fill(row, value);
        }
    }
}
