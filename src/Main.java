
public class Main {

    public static void main(String[] args) {
        int P = 8;
        int N = 80;

        StarNetwork network = new StarNetwork(P);

        ComputeNode[] nodes = new ComputeNode[P];
        for (int i = 0; i < P; i++) {
            int rank = i + 1;
            nodes[i] = new ComputeNode(rank, P, N, network);
            nodes[i].start();
        }

        for (int i = 0; i < P; i++) {
            try {
                nodes[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Всі обчислення завершено!");
    }
}