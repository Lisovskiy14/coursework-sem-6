package util;

public class MathUtils {

    public static long[] multiplyVectorByMatrix(long[] vector, long[][] matrix) {
        int A = vector.length;
        int B = matrix[0].length;

        long[] result = new long[B];

        for (int i = 0; i < B; i++) {
            long sum = 0;
            for (int j = 0; j < A; j++) {
                sum += vector[j] * matrix[j][i];
            }
            result[i] = sum;
        }

        return result;
    }

    public static long[][] multiplyMatrixBlocks(long[][] MA, long[][] MB) {
        int A = MA.length;
        int B = MB.length;

        long[][] result = new long[A][B];

        for (int i = 0; i < A; i++) {
            for (int k = 0; k < B; k++) {
                long sum = 0;
                for (int j = 0; j < B; j++) {
                    sum += MA[i][k] * MB[k][j];
                }
                result[i][k] = sum;
            }
        }

        return result;
    }

    public static long[] sumVectors(long[] vector, long[][] otherVectors) {
        int N = vector.length;
        long[] result = new long[N];

        System.arraycopy(vector, 0, result, 0, N);

        for (long[] otherVector : otherVectors) {
            if (otherVector != null) {
                for (int i = 0; i < N; i++) {
                    result[i] += otherVector[i];
                }
            }
        }

        return result;
    }

    public static long findMin(long[] vector) {
        long min = Long.MAX_VALUE;

        for (long value : vector) {
            if (value < min) {
                min = value;
            }
        }

        return min;
    }

    public static long findMax(long[] vector) {
        long max = Long.MIN_VALUE;

        for (long value : vector) {
            if (value > max) {
                max = value;
            }
        }

        return max;
    }

    public static long findGlobalMin(long globalMin, long[] vector) {
        for (long value : vector) {
            if (value < globalMin) {
                globalMin = value;
            }
        }

        return globalMin;
    }
}
