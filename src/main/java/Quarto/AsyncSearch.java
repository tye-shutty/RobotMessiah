package Quarto;

public class AsyncSearch extends Thread{
    RobotMessiah rm;
    boolean debug;
    boolean pieceAgent;
    byte agent;
    GState ns;
    byte piece;
    int recursion;
    float win =-1000;
    int counter = 0;
    private final Object lock = new Object();

    public float getWin(){
        synchronized (lock) {
            float temp = win;
            return temp;
        }
    }

    public AsyncSearch(RobotMessiah rm, boolean pieceAgent, byte agent, GState ns, byte piece, int recursion, boolean debug){
        counter++;
        //Common.prnRed("new thread="+currentThread().getId()+"; count="+counter);
        this.rm = rm;
        this.pieceAgent = pieceAgent;
        this.agent = agent;
        this.ns = ns;
        this.piece = piece;
        this.recursion = recursion;
        this.debug = debug;
    }
    
    public void run(){
        synchronized (lock) {
            if(pieceAgent){
                win = rm.heurRandPiece(agent, ns, recursion+1, debug); //chose different pieces?
            }
            else{
                win = rm.heurRandMove((byte)(-1*agent), ns, piece, recursion+1, debug);
            }
            //Common.prn("finished, win="+win);
            rm.incCount();
        }
    }
}
