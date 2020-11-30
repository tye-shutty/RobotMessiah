package Quarto;

public class ThreadState{
    boolean pieceAgent;
    byte agent;
    GState ns;
    byte piece;
    int recursion;
    float win;
    
    public ThreadState(boolean pieceAgent, byte agent, GState ns, byte piece, int recursion){
        this.pieceAgent = pieceAgent;
        this.agent = agent;
        this.ns = ns;
        this.piece = piece;
        this.recursion = recursion;
    }
}