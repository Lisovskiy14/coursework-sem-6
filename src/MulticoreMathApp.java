/*
 * Курсова робота з дисципліни «Архітектура комп'ютерів»
 * Тема: Розробка програмного забезпечення для комп'ютерів за багатоядерною архітектурою
 * Розділ 2. ПРГ1
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
public class MulticoreMathApp {
    public static void main(String[] args) {
        try {
            int n = 1456;
            int p = 16;

            if (n <= 0 || p <= 0 || n % p != 0) {
                throw new IllegalArgumentException("N та P мають бути додатними!\nN має ділитися на P без остачі!");
            }

            SharedDataMonitor monitor = new SharedDataMonitor(n, p);
            SyncCoordinator coordinator = new SyncCoordinator(p);
            ComputeThread[] threads = new ComputeThread[p];

            System.out.println("ПРГ1");
            System.out.println("Початок виконання паралельних обчислень");
            System.out.println("Формула: a = min(C*MZ) + max(D*(MX*MR))");
            System.out.println("Вхідні дані:");
            System.out.println("Розмірність N: " + n);
            System.out.println("Кількість потоків P: " + p);

            long startTime = System.nanoTime();

            // Створення та запуск потоків
            for (int i = 0; i < p; i++) {
                threads[i] = new ComputeThread(i, monitor, coordinator);
                threads[i].start();
            }

            // Очікування на завершення роботи
            for (ComputeThread thread : threads) {
                thread.join();
            }

            long endTime = System.nanoTime();
            double totalTime = (endTime - startTime) / 1000000.0;

            System.out.println("Всі потоки завершили роботу!");
            System.out.printf("Загальний час: %.3f мс\n", totalTime);

        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            System.out.println("Роботу головного потоку перервано.");
            ex.printStackTrace();
        }
    }
}