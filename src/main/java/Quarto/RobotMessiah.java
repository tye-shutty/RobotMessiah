package Quarto;

//Author: Tye Shutty
//Adapted from Michael Flemming's code
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

//maybe override choosePieceTurn(), etc to make more usable arrays 
public class RobotMessiah extends QuartoAgent {

    public GState currState = new GState();
    // variable to test different heuristics
    public Heuristic h = new LogicHeuristic();
    public LogicHeuristic lh = new LogicHeuristic();
    public final int MC_LIMIT = 10;

    public RobotMessiah(GameClient gameClient, String stateFileName) {
        super(gameClient, stateFileName); // does error checks
        currState = new GState();

        if (stateFileName != null) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(stateFileName));
                String line;
                byte row = 0;

                while ((line = br.readLine()) != null && row < 5) {
                    String[] splitted = line.split("\\s+");
                    for (byte col = 0; col < 5; col++) {

                        if (!splitted[col].equals("null")) {
                            currState.board[row][col] = 0; // -1 -> 0
                            for (byte i = 0; i < 5; i++) {

                                if (splitted[col].charAt(4 - i) == '1') {
                                    currState.board[row][col] = (byte) (currState.board[row][col] | (1 << i));
                                }
                            }
                            currState.pieces[Integer.parseInt(splitted[col], 2)][0] = row;
                            currState.pieces[Integer.parseInt(splitted[col], 2)][1] = col;
                            // currState.numPlayed++;
                        }
                    }
                    row++;
                }
                br.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("RM Error parsing quarto File");
            }
        }
    }

    public static void main(String[] args) {
        GameClient gameClient = new GameClient();
        String ip = null;
        String stateFileName = null;
        if (args.length > 0) {
            ip = args[0];
        } else {
            System.out.println("No IP Specified");
            System.exit(0);
        }
        if (args.length > 1) {
            stateFileName = args[1];
        }
        gameClient.connectToServer(ip, 4321);
        RobotMessiah quartoAgent = new RobotMessiah(gameClient, stateFileName);
        quartoAgent.play();
        gameClient.closeConnection();
    }

    @Override
    protected String pieceSelectionAlgorithm() {
        // if(currState.numPlayed == 0){
        // return random piece
        // }
        // this.startTimer();
        boolean skip = false;
        for (int i = 0; i < this.quartoBoard.getNumberOfPieces(); i++) {
            skip = false;
            if (!this.quartoBoard.isPieceOnBoard(i)) {
                for (int row = 0; row < this.quartoBoard.getNumberOfRows(); row++) {
                    for (int col = 0; col < this.quartoBoard.getNumberOfColumns(); col++) {
                        if (!this.quartoBoard.isSpaceTaken(row, col)) {
                            QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
                            copyBoard.insertPieceOnBoard(row, col, i);
                            if (copyBoard.checkRow(row) || copyBoard.checkColumn(col) || copyBoard.checkDiagonals()) {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if (skip) {
                        break;
                    }
                }
                if (!skip) {
                    return String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0');
                }
            }
            if (this.getMillisecondsFromTimer() > (this.timeLimitForResponse - COMMUNICATION_DELAY)) {
                // handle for when we are over some imposed time limit(make sure you account for
                // communication delay)
            }
            String message = null;
        }
        // if we don't find a piece in the above code just grab the first random piece
        int pieceId = this.quartoBoard.chooseRandomPieceNotPlayed(100);
        String BinaryString = String.format("%5s", Integer.toBinaryString(pieceId)).replace(' ', '0');

        return BinaryString;
    }

    private final Object lock = new Object();
    int counter = 0;

    public int incCount() {
        synchronized (lock) {
            return ++counter;
        }
    }

    // 0 is min wins=-1, tie=0, max wins=1; 1&2 are move coords resulting in best
    // outcome, -1 if no move.
    // agent is -1 if min agent, 1 if max
    public float[] bestMove(byte agent, GState s, byte piece, int recursion, int limit, boolean debug) {
        // for all moves, call bestPiece with each, unless only one move left or won
        float nextBest[] = { -2, -1, -1 }; // pos 0 is best MC or tie
        // for all moves (null spots), call bestPiece 
        if (recursion == 0) { // spawn threads
            int numOpen = 0;
            GState[] bestMoves = new GState[25]; // contains moves to spawn threads on
            byte[] bestMovesPos = new byte[25];
            for (byte k = 0; k < 25; k++) {
                if (s.board[k/5][k%5] == -1) {
                    GState t = s.copy();
                    t.board[k/5][k%5] = piece;
                    t.pieces[piece][0] = (byte)(k/5);
                    t.pieces[piece][1] = (byte)(k%5);
                    bestMovesPos[numOpen] = k;
                    bestMoves[numOpen] = t;
                    numOpen++;
                }
            }
            if(true){
            // Spawn 7 threads, find the best move
            AsyncSearch2 t[] = new AsyncSearch2[8];
            int minThreads = numOpen > 8 ? 8 : numOpen;
            int leftOvers = numOpen % 8;
            int numRemaining = numOpen;
            byte[] pArr = new byte[1]; //not used -> {1}?
            pArr[0] = -1;
            for (int i = 0; i < minThreads - 1; i++) {
                // give new thread array and length of subsequence
                int len = numOpen / 8 + ((leftOvers > 0) ? 1 : 0);
                GState[] newArr = new GState[len];
                leftOvers--;
                System.arraycopy(bestMoves, numOpen - numRemaining, newArr, 0, len);
                numRemaining -= len;
                
                t[i] = new AsyncSearch2(this, true, agent, newArr, pArr, recursion, limit, debug);
                new Thread(t[i]).start();
            }
            int len = numOpen / 8 + ((leftOvers > 0) ? 1 : 0);
            GState[] newArr = new GState[len];
            System.arraycopy(bestMoves, numOpen - numRemaining, newArr, 0, len);
            t[7] = new AsyncSearch2(this, true, agent, newArr, pArr, recursion, limit, debug);
            t[7].run(); // give main thread something to do
            float[] res = t[7].win;
            float nextWin = res[0];
            byte bestMove = (byte)(numOpen - numRemaining);  //first index
            for (int i = 1; i < res.length; i++) {
                if (res[i] * agent > nextWin * agent) {
                    nextWin = res[i];
                    bestMove = (byte)(i + numOpen - numRemaining);
                }
            }
            numRemaining = numOpen;
            for (int x = 0; x < minThreads - 1; x++) {
                try {
                    t[x].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                res = t[x].getWin();
                for(byte i=0; i<res.length; i++){
                    if (res[i]*agent>nextWin*agent){
                        nextWin = res[i];
                        bestMove = (byte)(numOpen-numRemaining);
                    }
                    numRemaining--;
                }
            }
            float[] a = {nextWin, bestMovesPos[bestMove]/5, bestMovesPos[bestMove]%5};
            return a;
        }
        }
        for (byte i = 0; i < 5; i++) {
            for (byte j = 0; j < 5; j++) {

                if (s.board[i][j] == -1) {
                    // copy game state, add piece to null position
                    GState ns = s.copy();
                    ns.board[i][j] = piece;
                    ns.pieces[piece][0] = i;
                    ns.pieces[piece][1] = j;

                    float win = 0;
                    if (recursion >= limit) {
                        for (int k = 0; k < MC_LIMIT; k++) {
                            win += heurLoop(true, agent, ns, (byte) -1, 1 + recursion, debug);
                        }
                        Common.prn("MC (move) finished, win="+win);
                        win /= MC_LIMIT;
                    } else {
                        win = bestPiece(agent, ns, 1 + recursion, limit, debug)[0]; // agent doesn't change
                    }
                    // Common.prn("piece="+piece+"; move="+i+","+j+"="+win);
                    if (win == agent) {
                        Common.prnRed("agent " + agent + " win at move " + i + ", " + j + ". Recursion=" + recursion);
                        float[] t = { win, i, j };
                        return t;
                    } else if (nextBest[0] < win * agent) {
                        // Common.prn("agent "+agent+" found tie at move "+i+", "+j+".
                        // Recursion="+recursion);
                        nextBest[0] = win;
                        nextBest[1] = i;
                        nextBest[2] = j;
                    } else {
                        // Common.prn("agent "+agent+" found nothing at move "+i+", "+j+".
                        // Recursion="+recursion);
                    }
                }
            }

        }
        nextBest[0] *= agent;
        return nextBest;
    }

    // 0 is min wins=-1, tie=0, max wins=1; 1 is piece resulting in best outcome, -1
    // if no piece.
    // float values between -1 and 1 represent value of monte carlo
    // limit must be > 0
    public float[] bestPiece(byte agent, GState s, int recursion, int limit, boolean debug) {
        float win = lh.win(s);
        float nextBest[] = { -2, -1 }; // pos 0 is best MC or tie
        if (win == 1) { // prev player has won
            if (debug)
                Common.prnRed("agent " + agent + " lost at recursion=" + recursion);
            float[] t = { (byte) (agent * -1), (byte) -1 };
            return t;
        } else if (win == -1) { // no win possible
            if (debug)
                Common.prnRed("agent " + agent + " tied at recursion=" + recursion);
            float[] t = { 0, (byte) -1 };
            return t;
        } else if (win == -2) { // loss inescapable
            if (debug)
                Common.prnRed("agent " + agent + " will lose at recursion=" + recursion);
            float[] t = { (byte) (agent * -1), (byte) -1 };
            return t;
        } else if (recursion == 0 && true) { // spawn threads
            int numUnplayed = 0;
            byte[] bestPieces = new byte[32]; // contains pieces to spawn threads on
            // for all unplayed pieces, call bestMove with each
            for (byte k = 0; k < 32; k++) {
                if (s.pieces[k][0] == -1) {
                    bestPieces[numUnplayed] = k;
                    numUnplayed++;
                }
            }
            // Spawn 7 threads, find the best piece
            AsyncSearch2 t[] = new AsyncSearch2[8];
            int minThreads = numUnplayed > 8 ? 8 : numUnplayed;
            int leftOvers = numUnplayed % 8;
            int numRemaining = numUnplayed;
            GState[] sArr = new GState[1];
            sArr[0] = s;
            for (int i = 0; i < minThreads - 1; i++) {
                // give new thread array and length of subsequence
                int len = numUnplayed / 8 + ((leftOvers > 0) ? 1 : 0);
                byte[] newArr = new byte[len];
                leftOvers--;
                System.arraycopy(bestPieces, numUnplayed - numRemaining, newArr, 0, len);
                numRemaining -= len;
                
                t[i] = new AsyncSearch2(this, true, agent, sArr, newArr, recursion, limit, debug);
                new Thread(t[i]).start();
            }
            int len = numUnplayed / 8 + ((leftOvers > 0) ? 1 : 0);
            byte[] newArr = new byte[len];
            System.arraycopy(bestPieces, numUnplayed - numRemaining, newArr, 0, len);
            t[7] = new AsyncSearch2(this, true, agent, sArr, newArr, recursion, limit, debug);
            t[7].run(); // give main thread something to do

            float[] res = t[7].win;
            float nextWin = res[0];
            byte bestPiece = (byte)(numUnplayed - numRemaining);
            for (int i = 1; i < res.length; i++) {
                if (res[i] * agent > nextWin * agent) {
                    nextWin = res[i];
                    bestPiece = (byte)(i + numUnplayed - numRemaining);
                }
            }
            numRemaining = numUnplayed;
            for (int x = 0; x < minThreads - 1; x++) {
                try {
                    t[x].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                res = t[x].getWin();
                for(byte i=0; i<res.length; i++){
                    if (res[i]*agent>nextWin*agent){
                        nextWin = res[i];
                        bestPiece = (byte)(numUnplayed - numRemaining);
                    }
                    numRemaining--;
                }
            }
            float[] a = {nextWin, bestPieces[bestPiece]};
            return a;
        } else{
            //for all unplayed pieces, call bestMove with each
            for(byte k=0; k< 32; k++){
                if(s.pieces[k][0] == -1){
                    if(recursion>=limit){
                        win = 0;
                        for(int i=0; i<MC_LIMIT; i++){
                            win += heurLoop(false, (byte)(-1*agent), s, k, 1+recursion, debug);
                        }
                        win /= MC_LIMIT;
                        Common.prn("MC finished, win="+win);
                    } else{
                        win = bestMove((byte)(-1*agent), s, k, 1+recursion, limit, debug)[0];
                    }
                    if(win == agent){
                        //Common.prnRed("agent "+agent+" win at piece "+k+". Recursion="+recursion);
                        float[] t = {win, k};
                        return t;
                    } else if(win*agent>nextBest[0]){
                        //Common.prn("agent "+agent+" found tie at piece "+k+". Recursion="+recursion);
                        nextBest[0] = win;
                        nextBest[1] = k;
                    } else{
                        //Common.prn("agent "+agent+" found nothing at piece "+k+". Recursion="+recursion);
                    }
                }
            }
        }
        nextBest[0] *= agent;
        return nextBest;
    }
    //combines heurRandPiece and heurRandMove to eliminate recursion
    //reduces time of 1000 iterations by ~0.6s = ~5%
    public byte heurLoop(boolean pieceAlg, byte agent, GState s, byte piece, int recursion, boolean debug){
        while(true){
            if(pieceAlg){ //pick a piece
                //shuffle all pieces
                int[] res = s.randPieces();
                byte bestPieceOutcome = -1; //opp wins; 0 forced tie, 1 neutral, 2 next maybe wins
                byte bestPiece = -1;
                boolean exit = false;
                //for all pieces
                for(byte k=0; k< 32 && !exit; k++){
                    //if unplayed
                    if(s.pieces[res[k]][0] == -1){
                        //shuffle all moves
                        int[] res2 = s.randMoves();
                        byte worstMoveOutcome = 2; //-1 opp wins; 0 forced tie, 1 neutral, 2 next maybe wins
                        boolean exitMove = false;
                        //for all moves
                        for(byte i=0; i<5 & !exitMove;i++){
                            for(byte j=0; j<5 & !exitMove; j++){
                                //if empty, call heuristic on state with piece inserted
                                if(s.board[res2[i*5+j]/5][res2[i*5+j]%5] == -1){
                                    GState ns = s.copy();
                                    ns.board[res2[i*5+j]/5][res2[i*5+j]%5] = (byte)res[k];
                                    ns.pieces[res[k]][0]= (byte)(res2[i*5+j]/5);
                                    ns.pieces[res[k]][1]= (byte)(res2[i*5+j]%5);
                                    
                                    byte winH = h.win(ns);
                                    if(winH == 1){
                                        // don't give this piece
                                        worstMoveOutcome = -1;
                                        exitMove = true;
                                    } else if(worstMoveOutcome>-1 && winH == -1){
                                        //tie better than loss
                                        worstMoveOutcome =0;
                                    } else if(worstMoveOutcome>0 && winH ==0){
                                        //neutral
                                        worstMoveOutcome =1;
                                    } else if(worstMoveOutcome==2 && winH == -2){
                                        //might be too rare to be worth checking
                                        worstMoveOutcome =2;
                                    }
                                }
                            }
                        }
                        if(worstMoveOutcome>bestPieceOutcome){
                            bestPieceOutcome = worstMoveOutcome;
                            bestPiece = (byte)res[k];
                        }
                        if(bestPieceOutcome==2){
                            //might be too rare, exit with >0 instead?
                            exit=true;
                        }
                    }
                }
                if(bestPieceOutcome == -1){
                    if(debug){
                        Common.prnYel("piece agent "+agent+" lost at recursion "+recursion);
                        Common.prn("piece "+bestPiece);
                        Common.prn(s.toString());
                    }
                    return (byte)(agent*-1);
                } else if(bestPieceOutcome==0){
                    if (debug){
                        Common.prnYel("piece agents tied at recursion "+recursion);
                        Common.prn("piece "+bestPiece);
                        Common.prn(s.toString());
                    }
                    return 0;
                } else{
                    agent = (byte)(-1*agent);
                    ++recursion;
                    piece = bestPiece;
                    pieceAlg = !pieceAlg;
                }
            }
            else{
                //for all moves, call bestPiece with each, unless only one move left or won
                int[] res = s.randMoves();
                byte bestMoveOutcome = -1; //-1 opp wins; 0 forced tie, 1 neutral, 2 I win
                GState bestMove = s.copy();
                int bestMovePos = 0;
                boolean exitMove = false;
                for(byte i=0; i<5 & !exitMove;i++){
                    for(byte j=0; j<5 & !exitMove; j++){
        
                        if(s.board[res[i*5+j]/5][res[i*5+j]%5] == -1){
                            //copy game state, add piece to null position
                            GState ns = s.copy();
                            ns.board[res[i*5+j]/5][res[i*5+j]%5] = piece;
                            ns.pieces[piece][0]= (byte)(res[i*5+j]/5);
                            ns.pieces[piece][1]= (byte)(res[i*5+j]%5);
                            
                            byte winH = h.win(ns);
                            if(winH == 1){
                                // I win
                                bestMoveOutcome = 2;
                                bestMove = ns;
                                exitMove = true;
                                bestMovePos = res[i*5+j];
                            }else if(bestMoveOutcome<0 && winH ==0){
                                //neutral
                                bestMove = ns;
                                bestMoveOutcome =1;
                            } else if(bestMoveOutcome<0 && winH == -1){
                                //tie better than loss
                                bestMove = ns;
                                bestMoveOutcome =0;
                                bestMovePos = res[i*5+j];
                            }
                        }
                    }
                }
                if(bestMoveOutcome == -1){
                    if(debug){
                        Common.prnRed("move agent "+agent+" lost at recursion "+recursion);
                        Common.prn("move "+bestMovePos/5+", "+bestMovePos%5+", "+piece);
                        Common.prn(bestMove.toString());
                    }
                    return (byte)(agent*-1);
                } else if(bestMoveOutcome==0){
                    if (debug){
                        Common.prnRed("move agents tied at recursion "+recursion);
                        Common.prn("move "+bestMovePos/5+", "+bestMovePos%5+", "+piece);
                        Common.prn(bestMove.toString());
                    }
                    return 0;
                } else{
                    //return heurRandPiece(agent, bestMove, 1+recursion, debug);
                    s=bestMove;
                    ++recursion;
                    pieceAlg = !pieceAlg;
                }
            }
        }
    }
    //returns: min wins=-1, tie=0, max wins=1
    //attempts to avoid immediate loss using win()
    public byte heurRandPiece(byte agent, GState s, int recursion, boolean debug){
        //shuffle all pieces
        int[] res = s.randPieces();
        byte bestPieceOutcome = -1; //opp wins; 0 forced tie, 1 neutral, 2 next maybe wins
        byte bestPiece = -1;
        boolean exit = false;
        //for all pieces
        for(byte k=0; k< 32 && !exit; k++){
            //if unplayed
            if(s.pieces[res[k]][0] == -1){
                //shuffle all moves
                int[] res2 = s.randMoves();
                byte worstMoveOutcome = 2; //-1 opp wins; 0 forced tie, 1 neutral, 2 next maybe wins
                boolean exitMove = false;
                //for all moves
                for(byte i=0; i<5 & !exitMove;i++){
                    for(byte j=0; j<5 & !exitMove; j++){
                        //if empty, call heuristic on state with piece inserted
                        if(s.board[res2[i*5+j]/5][res2[i*5+j]%5] == -1){
                            GState ns = s.copy();
                            ns.board[res2[i*5+j]/5][res2[i*5+j]%5] = (byte)res[k];
                            ns.pieces[res[k]][0]= (byte)(res2[i*5+j]/5);
                            ns.pieces[res[k]][1]= (byte)(res2[i*5+j]%5);
                            
                            byte winH = h.win(ns);
                            if(winH == 1){
                                // don't give this piece
                                worstMoveOutcome = -1;
                                exitMove = true;
                            } else if(worstMoveOutcome>-1 && winH == -1){
                                //tie better than loss
                                worstMoveOutcome =0;
                            } else if(worstMoveOutcome>0 && winH ==0){
                                //neutral
                                worstMoveOutcome =1;
                            } else if(worstMoveOutcome==2 && winH == -2){
                                //might be too rare to be worth checking
                                worstMoveOutcome =2;
                            }
                        }
                    }
                }
                if(worstMoveOutcome>bestPieceOutcome){
                    bestPieceOutcome = worstMoveOutcome;
                    bestPiece = (byte)res[k];
                }
                if(bestPieceOutcome==2){
                    //might be too rare, exit with >0 instead?
                    exit=true;
                }
            }
        }
        if(bestPieceOutcome == -1){
            if(debug){
                Common.prnYel("piece agent "+agent+" lost at recursion "+recursion);
                Common.prn("piece "+bestPiece);
                Common.prn(s.toString());
            }
            return (byte)(agent*-1);
        } else if(bestPieceOutcome==0){
            if (debug){
                Common.prnYel("piece agents tied at recursion "+recursion);
                Common.prn("piece "+bestPiece);
                Common.prn(s.toString());
            }
            return 0;
        } else{
            return heurRandMove((byte)(-1*agent), s, bestPiece, 1+recursion, debug);
        }
    }
    public byte heurRandMove(byte agent, GState s, byte piece, int recursion, boolean debug){
        //for all moves, call bestPiece with each, unless only one move left or won
        int[] res = s.randMoves();
        byte bestMoveOutcome = -1; //-1 opp wins; 0 forced tie, 1 neutral, 2 I win
        GState bestMove = s.copy();
        int bestMovePos = 0;
        boolean exitMove = false;
        for(byte i=0; i<5 & !exitMove;i++){
            for(byte j=0; j<5 & !exitMove; j++){

                if(s.board[res[i*5+j]/5][res[i*5+j]%5] == -1){
                    //copy game state, add piece to null position
                    GState ns = s.copy();
                    ns.board[res[i*5+j]/5][res[i*5+j]%5] = piece;
                    ns.pieces[piece][0]= (byte)(res[i*5+j]/5);
                    ns.pieces[piece][1]= (byte)(res[i*5+j]%5);
                    
                    byte winH = h.win(ns);
                    if(winH == 1){
                        // I win
                        bestMoveOutcome = 2;
                        bestMove = ns;
                        exitMove = true;
                        bestMovePos = res[i*5+j];
                    }else if(bestMoveOutcome<0 && winH ==0){
                        //neutral
                        bestMove = ns;
                        bestMoveOutcome =1;
                    } else if(bestMoveOutcome<0 && winH == -1){
                        //tie better than loss
                        bestMove = ns;
                        bestMoveOutcome =0;
                        bestMovePos = res[i*5+j];
                    }
                }
            }
        }
        if(bestMoveOutcome == -1){
            if(debug){
                Common.prnRed("move agent "+agent+" lost at recursion "+recursion);
                Common.prn("move "+bestMovePos/5+", "+bestMovePos%5+", "+piece);
                Common.prn(bestMove.toString());
            }
            return (byte)(agent*-1);
        } else if(bestMoveOutcome==0){
            if (debug){
                Common.prnRed("move agents tied at recursion "+recursion);
                Common.prn("move "+bestMovePos/5+", "+bestMovePos%5+", "+piece);
                Common.prn(bestMove.toString());
            }
            return 0;
        } else{
            return heurRandPiece(agent, bestMove, 1+recursion, debug);
        }
    }
    //returns sum of all win possibilities. For every row/col/diag check if 5 matching chars is possible
    //can consider different rows aside from different chars in same row
    //counts num pieces that can be used to win?
    /**
    public int winOpenings(state s){
        int[] winPossibilities = {0,0,0}; //1 first is all unique, 2 is just separate rows/col/diag
        //3 is num pieces that can lead to win
        //check rows and columns for win possibility(excludes existing peices)
        boolean pivot = false;

        for(byte i=0; i<5; i++){
            byte nullCount = 0;
            byte[] currChars = {0,0,0,0,0};
            byte[][] charWins = {{0,0,0,0,0},{0,0,0,0,0}}; //0 is 0 wins, 1 is 1 wins
            //for each diff num of null spots, which chars needed to win?

            for(byte j=0; j<5; j++){
                if(pivotLookup(s.board, pivot,i,j) == -1){
                    nullCount++;
                } else{
                    byte rc = pivotLookup(s.board, pivot,i,j);
                    for(byte z=0;z<5;z++){
                        currChars[4-z] += 1 | (byte)(rc >> z);
                    }
                }
            }
            for(int j=0; j<5; j++){
                for(byte k=0;k<32;k++){
                    for(byte a=0;a<2;a++){
                        
                    }
                }
                if(currChars[j] == 0 || currChars[j]+nullCount == 5){
                    winPossibilities++;
                }
            }

            if(i==4 && pivot == false){
                i=-1;
                pivot=true;
            }
        }
        //check diagonals
        for(int[] i={0,1}; i[1]>-3; i[0]=4, i[1]-=2){
            int[] currChars ={0,0,0,0,0};
            int nullCount = 0;
            for(int j=0; j<5; j++){
                if(s.board[j][i[0]+i[1]*j][0] == -1){
                    nullCount++;
                } else{
                    int[] rc = s.board[j][i[0]+i[1]*j];
                    for(int z=0;z<5;z++){
                        currChars[z] += rc[z];
                    }
                }
            }
            for(int j=0; j<5; j++){
                if(currChars[j] == 0 || currChars[j]+nullCount == 5){
                    winPossibilities++;
                }
            }
        }
        return winPossibilities;
    }
     */
    @Override
    protected String moveSelectionAlgorithm(int pieceID){
        /*
        this.startNanoTimer();
        QuartoPiece curr = this.quartoBoard.getPiece(pieceID);
        int[] chars ={0,0,0,0,0};
        for(int j=0; j<5; j++){
            chars[j] = curr.getCharacteristicsArray()[j] ? 1 : 0;
        }

        //check rows and columns for a winning move
        boolean pivot = false;
        for(int i=0; i<5; i++){
            int nullCount = 0;
            int[] currChars = new int[5];
            System.arraycopy(chars, 0, currChars, 0, 5);
            int nullj = -1;
            for(int j=0; j<5; j++){
                if(pivotLookup(this.quartoBoard.board, pivot,i,j) == null){
                    nullCount++;
                    nullj = j;
                    if(nullCount > 1){
                        break;
                    }
                } else{
                    boolean[] rc = pivotLookup(this.quartoBoard.board, pivot,i,j).getCharacteristicsArray();
                    for(int z=0;z<5;z++){
                        currChars[z] += rc[z] ? 1 : 0;
                    }
                }
            }
            if(nullCount == 1){
                for(int j=0; j<5; j++){
                    if(currChars[j] == 0 || currChars[j] == 5){
                        Common.prnRed("row or col solution: "+this.getNanosecondsFromTimer()+"ns");
                        return pivot ? nullj+","+i : i+","+nullj;
                    }
                }
            }
            if(i==4 && pivot == false){
                i=-1;
                pivot=true;
            }
        }
        //check diagonals
        for(int[] i={0,1}; i[1]>-3; i[0]=4, i[1]-=2){
            int[] currChars = chars;
            int nullCount = 0;
            int nullj = -1;
            for(int j=0; j<5; j++){
                if(this.quartoBoard.board[j][i[0]+i[1]*j] == null){
                    nullCount++;
                    nullj = j;
                    if(nullCount > 1){
                        break;
                    }
                } else{
                    boolean[] rc = this.quartoBoard.board[j][i[0]+i[1]*j].getCharacteristicsArray();
                    for(int z=0;z<5;z++){
                        currChars[z] += rc[z] ? 1 : 0;
                    }
                }
            }
            if(nullCount == 1){
                for(int j=0; j<5; j++){
                    if(currChars[j] == 0 || currChars[j] == 5){
                        Common.prnRed("diagonal solution: "+this.getNanosecondsFromTimer()+"ns");
                        return nullj+","+(i[0]+nullj*i[1]);
                    }
                }
            }
        }*/

        // If no winning move is found in the above code, then return a random(unoccupied) square
        int[] move = new int[2];
        QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
        move = copyBoard.chooseRandomPositionNotPlayed(100);

        //Common.prnRed("random solution: "+this.getNanosecondsFromTimer()+"ns");
        return move[0] + "," + move[1];
    }
}
