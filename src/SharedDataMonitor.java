import java.util.Arrays;

public class SharedDataMonitor {
    private final int n;
    private final int p;
    private final int h;

    private final long[] c;
    private final long[] d;
    private final long[][] mz;
    private final long[][] mx;
    private final long[][] mr;
    private final long[][] mq;

    private long m1;
    private long m2;
    private long a;

    public SharedDataMonitor(int n, int p) {
        this.n = n;
        this.p = p;
        this.h = n / p;

        this.c = new long[n];
        this.d = new long[n];
        this.mz = new long[n][n];
        this.mx = new long[n][n];
        this.mr = new long[n][n];
        this.mq = new long[n][n];

        this.m1 = Long.MAX_VALUE;
        this.m2 = Long.MIN_VALUE;
        this.a = 0L;
    }

    public int getBlockSize() {
        return h;
    }
    public int getThreadCount() {
        return p;
    }
    public int getSize() {
        return n;
    }

    public long[] getC() {
        return c;
    }
    public long[] getD() {
        return d;
    }
    public long[][] getMZ() {
        return mz;
    }
    public long[][] getMX() {
        return mx;
    }
    public long[][] getMR() {
        return mr;
    }
    public long[][] getMQ() {
        return mq;
    }

    public long getM1() {
        return m1;
    }
    public long getM2() {
        return m1;
    }
    public long getResultA() {
        return a;
    }

    public void initT1Data() {
        fillVector(c, 1L);
        fillMatrix(mx, 1L);
    }

    public void initTpData() {
        fillVector(d, 1L);
        fillMatrix(mr, 1L);
        fillMatrix(mz, 1L);
    }

    public synchronized void updateM1(long value) {
        if (value < m1) m1 = value;
    }

    public synchronized void updateM2(long value) {
        if (value > m2) m2 = value;
    }

    public synchronized void calculateFinalResult() {
        a = m1 + m2;
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
