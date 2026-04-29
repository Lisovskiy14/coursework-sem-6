public class Message {
    public int sourceRank;
    public int targetRank;
    public Object[] payload;

    public Message(int sourceRank, int targetRank, Object[] payload) {
        this.sourceRank = sourceRank;
        this.targetRank = targetRank;
        this.payload = payload;
    }
}
