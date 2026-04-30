package util;

public class MathUtils {

    public static long[] multiplyVectorByMatrix(long[] vector, long[] matrix, int vectorLength, int matrixColumns) {
        long[] result = new long[matrixColumns];

        for (int i = 0; i < vectorLength; i++) {
            long v = vector[i];
            for (int j = 0; j < matrixColumns; j++) {
                result[j] += v * matrix[i * matrixColumns + j];
            }
        }
        return result;
    }

    public static long[] multiplyMatrixBlocks(long[] MAh, long[] MB, int H, int N) {
        long[] result = new long[H * N];

        for (int i = 0; i < H; i++) {
            for (int k = 0; k < N; k++) {
                long temp = MAh[i * N + k];
                for (int j = 0; j < N; j++) {
                    result[i * N + j] += temp * MB[k * N + j];
                }
            }
        }
        return result;
    }

    public static long[] sumVectors(long[] vector, long[] allVectors, int P, int N) {
        long[] result = new long[N];
        System.arraycopy(vector, 0, result, 0, N);

        for (int p = 1; p < P; p++) { // Починаємо з 1, бо 0-й індекс порожній (це Т1)
            for (int i = 0; i < N; i++) {
                result[i] += allVectors[p * N + i];
            }
        }
        return result;
    }

    public static long findMin(long[] vector) {
        long min = Long.MAX_VALUE;
        for (long value : vector) {
            if (value < min) min = value;
        }
        return min;
    }

    public static long findMax(long[] vector) {
        long max = Long.MIN_VALUE;
        for (long value : vector) {
            if (value > max) max = value;
        }
        return max;
    }

    public static long findGlobalMin(long localMin, long[] allMins) {
        long globalMin = localMin;
        for (int i = 1; i < allMins.length; i++) {
            if (allMins[i] < globalMin) {
                globalMin = allMins[i];
            }
        }
        return globalMin;
    }
}
