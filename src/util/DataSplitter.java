package util;

import java.util.Arrays;

public class DataSplitter {

    public static long[] getVectorBlock(long[] vector, int p, int H) {
        return Arrays.copyOfRange(vector, p * H, (p + 1) * H);
    }

    public static long[] getRowBlock(long[] matrix, int p, int H, int N) {
        int startIndex = p * H * N;
        int endIndex = startIndex + (H * N);
        return Arrays.copyOfRange(matrix, startIndex, endIndex);
    }

    public static long[] getColumnBlock(long[] matrix, int p, int H, int N) {
        long[] result = new long[N * H];
        int startCol = p * H;

        for (int i = 0; i < N; i++) {
            System.arraycopy(matrix, i * N + startCol, result, i * H, H);
        }
        return result;
    }
}
