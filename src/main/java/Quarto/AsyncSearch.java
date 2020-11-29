package Quarto;

public class AsyncSearch extends Thread{
    ThreadLocal<RobotMessiah> rm;
    float win = -2;
    byte agent;
    GState ns;
    byte piece;
    int recursion;
    boolean debug;
    boolean pieceAgent;

    public AsyncSearch(RobotMessiah rm, boolean pieceAgent, byte agent, GState ns, byte piece, int recursion, boolean debug){
        this.rm = new ThreadLocal<rm>();
        this.pieceAgent = pieceAgent;
        this.agent = agent;
        this.ns = ns;
        this.piece = piece;
        this.recursion = recursion;
        this.debug = debug;
    }
    
    public void run(){
        if(pieceAgent)
            win= rm.heurRandPiece(agent, ns, recursion+1, debug); //chose different pieces?
        else
            win = rm.heurRandMove((byte)(-1*agent), ns, piece, recursion+1, debug);
    }
}
