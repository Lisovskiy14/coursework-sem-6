import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class SyncCoordinator {
    private final CountDownLatch inputLatch;
    private final CyclicBarrier matrixBarrier;
    private final CountDownLatch reductionLatch;

    public SyncCoordinator(int threadCount) {
        this.inputLatch = new CountDownLatch(2);
        this.matrixBarrier = new CyclicBarrier(threadCount);
        this.reductionLatch = new CountDownLatch(threadCount);
    }

    public void signalDataReady() {
        inputLatch.countDown();
    }

    public void waitForInput() throws InterruptedException {
        inputLatch.await();
    }

    public void waitForMatrixPhase() throws BrokenBarrierException, InterruptedException {
        matrixBarrier.await();
    }

    public void signalReductionDone() {
        reductionLatch.countDown();
    }

    public void waitForAllReductions() throws InterruptedException {
        reductionLatch.await();
    }
}
