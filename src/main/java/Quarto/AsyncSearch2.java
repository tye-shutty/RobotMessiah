package Quarto;

public class AsyncSearch2 extends Thread{
    RobotMessiah rm;
    boolean debug;
    boolean pieceAgent;
    byte agent;
    GState[] ns;
    byte[] piece;
    int recursion;
    int limit;
    float win[];
    private final Object lock = new Object();

    public float[] getWin(){
        synchronized (lock) {
            return win;  //shouldn't need to copy
        }
    }

    public AsyncSearch2(RobotMessiah rm, boolean pieceAgent, byte agent, GState[] ns, byte[] piece, 
    int recursion, int limit, boolean debug)
    {
        //Common.prnRed("new thread="+currentThread().getId()+"; count="+counter);
        this.rm = rm;
        this.pieceAgent = pieceAgent;
        this.agent = agent;
        this.ns = ns;
        this.piece = piece;
        this.recursion = recursion;
        this.limit = limit;
        this.debug = debug;

        win = new float[((piece.length > ns.length) ? piece.length : ns.length)];
    }
    
    public void run(){
        synchronized (lock) {
            if(pieceAgent){
                for(int i=0;i<ns.length; i++)
                    win[i] = rm.bestMove((byte)(-1*agent), ns[0], piece[i], 1+recursion, limit, debug)[0];
            }
            else{
                for(int i=0;i<ns.length; i++)
                    win[i] = rm.bestPiece(agent, ns[i], 1+recursion, limit, debug)[0];
            }
            //Common.prn("finished, win="+win);
            //rm.incCount();
        }
    }
}
