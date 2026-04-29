import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StarNetwork {
    private Map<Integer, BlockingQueue<Message>> queues = new HashMap<>();

    public StarNetwork(int numNodes) {
        for (int i = 1; i <= numNodes; i++) {
            queues.put(i, new LinkedBlockingQueue<>());
        }
    }

    public void send(Message message) {
        try {
            queues.get(message.targetRank).put(message);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public Message receive(int myRank) {
        try {
            return queues.get(myRank).take();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
