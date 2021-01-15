import java.util.Arrays;
public class AsyncSearch2 extends Thread{
    QuartoPlayerAgent rm;
    boolean debug;
    boolean pieceAgent;
    byte agent;
    GState[] ns;
    byte[] piece;
    int recursion;
    int limit;
    volatile float win[];
    private final Object lock = new Object();

    public float[] getWin(){
        synchronized (lock) {
            // float[] newArr = new float[win.length];
            // System.arraycopy(win, 0, newArr, 0, win.length);
            // return newArr;  //shouldn't need to copy?
            return win;
        }
    }

    public AsyncSearch2(QuartoPlayerAgent rm, boolean pieceAgent, byte agent, GState[] ns, byte[] piece, 
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
                for(int i=0;i<piece.length; i++){
                    //Common.prnYel("run piece="+piece[i]);
                    win[i] = rm.bestMove((byte)(-1*agent), ns[0], piece[i], recursion+1, limit, debug)[0];
                    //increase recursion, because this is like the last step of the pieceAgent,
                    //and recursion was not increased in the call to AsyncSearch2().
                }
            }
            else{
                for(int i=0;i<ns.length; i++)
                    win[i] = rm.bestPiece(agent, ns[i], recursion+1, limit, debug)[0];
            }
            //Common.prn("finished, win="+Arrays.toString(win));
            //rm.incCount();
        }
    }
}
