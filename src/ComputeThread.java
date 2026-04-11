import java.util.concurrent.BrokenBarrierException;

public class ComputeThread extends Thread {
    private final int threadId;
    private final SharedDataMonitor monitor;
    private final SyncCoordinator coordinator;
    private final int fromIndex;
    private final int toIndex;

    public ComputeThread(int threadId, SharedDataMonitor monitor, SyncCoordinator coordinator) {
        this.threadId = threadId;
        this.monitor = monitor;
        this.coordinator = coordinator;

        int h = monitor.getBlockSize();
        this.fromIndex = threadId * h;
        this.toIndex = fromIndex + h;

        setName("T" + (threadId + 1));
    }

    @Override
    public void run() {
        try {
            // ЕТАП 1: Введення даних
            if (threadId == 0) {
                monitor.initT1Data(); // Введення C, MX
                coordinator.signalDataReady(); // Сигнал про введення даних
            } else if (threadId == monitor.getThreadCount() - 1) {
                monitor.initTpData(); // Введення D, MR, MZ
                coordinator.signalDataReady(); // Сигнал про введення даних
            }

            coordinator.waitForInput(); // Усі потоки чекають на введення даних

            // ЕТАП 2: Перша фаза обчислень
            long localM1 = computeZAndFindM1(); // Обчислення локального m1
            computeMQBlock(); // Обчислення MQh

            // ЕТАП 3: Бар'єр
            coordinator.waitForMatrixPhase(); // Чекаємо поки усі потоки дорахують свої рядки MQ

            // ЕТАП 4: Друга фаза обчислень
            long localM2 = computeEAndFindM2(); // Обчислення локального m2

            // ЕТАП 5: Редукція (зведення результатів)
            monitor.updateM1(localM1); // Обчислення глобального m1
            monitor.updateM2(localM2); // Обчислення глобального m2
            coordinator.signalReductionDone(); // Сигнал про завершення фази редукції

            // ЕТАП 6: Виведення результату потоком Т1
            if (threadId == 0) {
                coordinator.waitForAllReductions(); // Очікуєм завершення фази редукції усіма потоками

                monitor.calculateFinalResult(); // Фінальне обчислення a

                printResults(); // Виведення результату
            }

        } catch (InterruptedException | BrokenBarrierException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Помилка виконання в потоці %s: %s".formatted(getName(), ex));
        }
    }


    // Допоміжні методи

    private long computeZAndFindM1() {
        long[] c = monitor.getC();
        long[][] mz = monitor.getMZ();
        int n = monitor.getSize();
        long minValue = Long.MAX_VALUE;

        for (int i = fromIndex; i < toIndex; i++) {
            long zElement = 0;
            for (int j = 0; j < n; j++) {
                zElement += c[j] * mz[j][i];
            }
            if (zElement < minValue) {
                minValue = zElement;
            }
        }

        return minValue;
    }

    private void computeMQBlock() {
        long[][] mx = monitor.getMX();
        long[][] mr = monitor.getMR();
        long[][] mq = monitor.getMQ();
        int n = monitor.getSize();

        for (int i = fromIndex; i < toIndex; i++) {
            for (int j = 0; j < n; j++) {
                long cell = 0;
                for (int k = 0; k < n; k++) {
                    cell += mx[i][k] * mr[k][j];
                }
                mq[i][j] = cell;
            }
        }
    }

    private long computeEAndFindM2() {
        long[] d = monitor.getD();
        long[][] mq = monitor.getMQ();
        int n = monitor.getSize();
        long maxValue = Long.MIN_VALUE;

        for (int i = fromIndex; i < toIndex; i++) {
            long eElement = 0;
            for (int j = 0; j < n; j++) {
                eElement += d[j] * mq[j][i];
            }
            if (eElement > maxValue) {
                maxValue = eElement;
            }
        }

        return maxValue;
    }

    private void printResults() {
        String template = """
                T1: Обчислення завершено.
                T1: Підсумковий результат a = %d
                """;
        System.out.printf(template, monitor.getResultA());
    }
}
