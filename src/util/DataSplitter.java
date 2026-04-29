package util;

import java.util.Arrays;

public class DataSplitter {

    public static long[] getVectorBlock(long[] vector, int p, int H) {
        int start = p * H;
        int end = start + H;
        return Arrays.copyOfRange(vector, start, end);
    }

    public static long[][] getMatrixRowBlock(long[][] matrix, int p, int H) {
        int start = p * H;
        int end = start + H;
        return Arrays.copyOfRange(matrix, start, end);
    }

    public static long[][] getMatrixColumnBlock(long[][] matrix, int p, int H) {
        int N = matrix.length;
        int start = p * H;

        long[][] result = new long[N][H];

        for (int i = 0; i < N; i++) {
            System.arraycopy(matrix[i], start, result[i], 0, H);
        }

        return result;
    }
}
