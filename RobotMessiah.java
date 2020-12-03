
//Author: Tye Shutty
//Adapted from Michael Flemming's code
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

//maybe override choosePieceTurn(), etc to make more usable arrays 
public class RobotMessiah extends QuartoAgent {

    public GState currState = new GState();
    // variable to test different heuristics
    public Heuristic h = new LogicHeuristic(); //for MC 
    public LogicHeuristic lh = new LogicHeuristic(); //for avoiding immediate loss
    public int mcLimit = 10;  //best if >= 3
    public int treeLimit = 3;  //always be >= 3
    public boolean globDebug = false;
    public boolean multithreaded = true;
    long timeStart;
    boolean timeout = false;

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
	protected void choosePieceTurn() {

		String MessageFromServer;
		MessageFromServer = this.gameClient.readFromServer(1000000);
		String[] splittedMessage = MessageFromServer.split("\\s+");

		//close program if message is not the expected message
		isExpectedMessage(splittedMessage, SELECT_PIECE_HEADER, true);
        //send piece to server
		String pieceMessage = pieceSelectionAlgorithm();
		this.gameClient.writeToServer(pieceMessage);
		MessageFromServer = this.gameClient.readFromServer(1000000);
		String[] splittedResponse = MessageFromServer.split("\\s+");
		if (!isExpectedMessage(splittedResponse, ACKNOWLEDGMENT_PIECE_HEADER) && 
			!isExpectedMessage(splittedResponse, ERROR_PIECE_HEADER)) 
		{
			turnError(MessageFromServer);
		}
        //receive move from server
		int pieceID = Integer.parseInt(splittedResponse[1], 2);
		MessageFromServer = this.gameClient.readFromServer(1000000);
		String[] splittedMoveResponse = MessageFromServer.split("\\s+");
		isExpectedMessage(splittedMoveResponse, MOVE_MESSAGE_HEADER, true);

		String[] moveString = splittedMoveResponse[1].split(",");
		int[] move = new int[2];
		move[0] = Integer.parseInt(moveString[0]);
		move[1] = Integer.parseInt(moveString[1]);

        //this.quartoBoard.insertPieceOnBoard(move[0], move[1], pieceID);
        currState.board[move[0]][move[1]] = (byte)pieceID;
        currState.pieces[pieceID][0] = (byte)move[0];
        currState.pieces[pieceID][1] = (byte)move[1];
    }
    @Override
	protected void chooseMoveTurn() {
		//get message
		String MessageFromServer;
		MessageFromServer = this.gameClient.readFromServer(1000000);
		String[] splittedMessage = MessageFromServer.split("\\s+");

		//close program if message is not the expected message
		isExpectedMessage(splittedMessage, SELECT_MOVE_HEADER, true);
		int pieceID = Integer.parseInt(splittedMessage[1], 2);

		//determine piece
		String moveMessage = moveSelectionAlgorithm(pieceID);
		this.gameClient.writeToServer(moveMessage);

		MessageFromServer = this.gameClient.readFromServer(1000000);
		String[] splittedMoveResponse = MessageFromServer.split("\\s+");
		if (!isExpectedMessage(splittedMoveResponse, ACKNOWLEDGMENT_MOVE_HEADER) && !isExpectedMessage(splittedMoveResponse, ERROR_MOVE_HEADER)) {
			turnError(MessageFromServer);
		}
		String[] moveString = splittedMoveResponse[1].split(",");
		int[] move = new int[2];
		move[0] = Integer.parseInt(moveString[0]);
		move[1] = Integer.parseInt(moveString[1]);

		//this.quartoBoard.insertPieceOnBoard(move[0], move[1], pieceID);
        currState.board[move[0]][move[1]] = (byte)pieceID;
        currState.pieces[pieceID][0] = (byte)move[0];
        currState.pieces[pieceID][1] = (byte)move[1];
	}
    @Override
    protected String pieceSelectionAlgorithm() {
        // if(currState.numPlayed == 0){
        // return random piece
        // }
        timeStart=System.currentTimeMillis();
        //Common.prn("start at="+timeStart);
        
        //count empty spaces/pieces to estimate max depth and max MC possible in 10s
        int numUnplayed = 0;
        for (byte k = 0; k < 32; k++) {
            if (currState.pieces[k][0] == -1) {
                numUnplayed++;
            }
        }
        //Common.prn(currState.toString());
        float res[] = bestPiece((byte)1, currState, 0, treeLimit, globDebug);
        Common.prn("res="+Arrays.toString(res));
        int pieceId = (int)res[1];  //what if -1?
        String BinaryString = String.format("%5s", Integer.toBinaryString(pieceId)).replace(' ', '0');
        //Common.prn("leaves="+counter);
        return BinaryString;
    }
    @Override
    protected String moveSelectionAlgorithm(int pieceID){
        //Common.prnRed("piece="+pieceID);
        timeStart=System.currentTimeMillis();
        Common.prn("start at="+timeStart);
        
        float[] move = bestMove((byte)1, currState, (byte)pieceID, 0, treeLimit, globDebug);
        return (int)move[1] + "," + (int)move[2];
    }
    // 0 is min wins=-1, tie=0, max wins=1; 1&2 are move coords resulting in best
    // outcome, -1 if no move.
    // agent is -1 if min agent, 1 if max
    // limit must be > 1 (MC loop level)
    //bestMove uses bestPiece to check immediate win states, so if limit=1, then bestMove won't see
    //immediate wins
    public float[] bestMove(byte agent, GState s, byte piece, int recursion, int limit, boolean debug) {
        //if(recursion==1)
            //incCount();
        // for all moves, call bestPiece with each, unless only one move left or won
        float nextBest[] = { -2, -1, -1 }; // pos 0 is best MC or tie
        //-0.2 represents timeout, may or may not have piece
        // for all moves (null spots), call bestPiece 
        if (recursion == 0 && multithreaded) { // spawn threads
            int numOpen = 0;
            GState[] bestMoves = new GState[25]; // contains moves to spawn threads on
            byte[] bestMovesPos = new byte[25];
            //byte bestMoveResult = -1; 
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
                
                t[i] = new AsyncSearch2(this, false, agent, newArr, pArr, recursion, limit, debug);
                debug = false; //depth only
                new Thread(t[i]).start();
            }
            int len = numOpen / 8 + ((leftOvers > 0) ? 1 : 0);
            GState[] newArr = new GState[len];
            System.arraycopy(bestMoves, numOpen - numRemaining, newArr, 0, len);
            t[7] = new AsyncSearch2(this, false, agent, newArr, pArr, recursion, limit, debug);
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
            float[] a = {nextWin, bestMovesPos[bestMove]/5, bestMovesPos[bestMove]%5}; //no alpha beta
            //because parallel
            return a;
        }
        for (byte i = 0; i < 5; i++) {
            for (byte j = 0; j < 5; j++) {
                // if(recursion==limit)
                //     incCount();
                if(timeout && recursion>2){
                    //Common.prnRed("timeout");
                    nextBest[0]=(float)-0.99; //slightly bad for max
                    return nextBest;
                }
                if (s.board[i][j] == -1) {
                    // copy game state, add piece to null position
                    GState ns = s.copy();
                    ns.board[i][j] = piece;
                    ns.pieces[piece][0] = i;
                    ns.pieces[piece][1] = j;

                    float win = 0;
                    if (recursion >= limit) {
                        win = initHeurLoop(true, agent, ns, (byte)-1, recursion+1, debug);
                        debug=false;
                        if(debug)
                            Common.prn("MC (move) finished, win="+win);
                    } else {
                        win = bestPiece(agent, ns, 1 + recursion, limit, debug)[0]; // agent doesn't change
                        // if(piece==3){
                        //     Common.prnRed("win with 3="+win+"\n"+ns.toString());
                        // }
                        debug = false;  //depth only debugging
                    }
                    // Common.prn("piece="+piece+"; move="+i+","+j+"="+win);
                    if (win == agent) {
                        if(debug)
                            Common.prnRed("agent " + agent + " win at move " + i + ", " + j + ". Recursion=" + recursion);
                        float[] t = { win, i, j };
                        // if(recursion==limit)
                        //     incCount2();
                        return t;
                    } else if (nextBest[0] < win * agent) {
                        // Common.prn("agent "+agent+" found tie at move "+i+", "+j+".
                        // Recursion="+recursion);
                        nextBest[0] = win;
                        nextBest[1] = i;
                        nextBest[2] = j;
                    }
                    if(recursion==0){
                        Common.prn("agent "+agent+" found win="+win+ " at move "+i+","+j+". Recursion="+recursion);
                    }
                }
            }
        }
        //nextBest[0] *= agent; NO, return winning agent!
        return nextBest;
    }

    // pos 0 is min wins=-1, tie=0, max wins=1; pos 1 is piece resulting in best outcome, -1
    // if no piece.
    // float values between -1 and 1 represent value of monte carlo
    // limit must be > 0
    public float[] bestPiece(byte agent, GState s, int recursion, int limit, boolean debug) {
        // if(recursion==1)
        //     incCount();
        float win = lh.win(s);
        float nextBest[] = { -2, -1 }; // pos 0 is best MC or tie
        if (win == 1) { // prev player (me) has won (took forever to debug this)
            if (debug)
                Common.prnRed("agent " + agent + " lost at recursion=" + recursion);
            float[] t = { (float)(agent*1.1), (byte) -1 }; //larger to break ties with MC
            return t;
        } else if (win == -1) { // no win possible
            if (debug)
                Common.prnRed("agent " + agent + " tied at recursion=" + recursion);
            float[] t = { 0, (byte) -1 };
            return t;
        } else if (win == -2) { // next player (opponent) will win
            if (debug)
                Common.prnRed("agent " + agent + " will lose at recursion=" + recursion);
            float[] t = { (byte)(agent*-1.1), (byte) -1 };
            return t;
        } else if(timeout && recursion>2){  // check in bestPiece, and bestMove
            //Common.prnRed("timeout");
            float[] t = { (float)-0.99, -1 }; //don't know if win or lose, just assume
            //slightly bad for max agent
            return t;
        } else if (recursion == 0 && multithreaded) { // spawn threads
            int numUnplayed = 0;
            byte[] bestPieces = new byte[32]; // contains pieces to spawn threads on
            
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
            //Common.prn("minThreads="+minThreads);
            for (int i = 0; i < minThreads - 1; i++) {
                // give new thread array and length of subsequence
                int len = numUnplayed / 8 + ((leftOvers > 0) ? 1 : 0);
                byte[] newArr = new byte[len];
                leftOvers--;
                System.arraycopy(bestPieces, numUnplayed - numRemaining, newArr, 0, len);
                numRemaining -= len;
                
                t[i] = new AsyncSearch2(this, true, agent, sArr, newArr, recursion, limit, debug);
                debug = false; // depth only
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
            float[] a = {nextWin, bestPieces[bestPiece]}; //no alpha beta because parallel
            if(debug && recursion==0){
                Common.prn("agent "+agent+" found win="+nextWin+ " at piece "+bestPieces[bestPiece]+". Recursion="+recursion);
            }
            return a;
        } else{
            //for all unplayed pieces, call bestMove with each
            for(byte k=0; k< 32; k++){
                if(s.pieces[k][0] == -1){
                    if(recursion>=limit){
                        //incCount();
                        //probably don't need to check timeout here
                        //incCount();
                        win = initHeurLoop(false, (byte)(-1*agent), s, k, 1+recursion, debug);
                        debug = false; //depth only
                        if(debug)
                            Common.prn("MC finished, win="+win);
                    } else{
                        win = bestMove((byte)(-1*agent), s, k, 1+recursion, limit, debug)[0];
                        debug = false; //depth only
                    }
                    if(win == agent){
                        //Common.prnRed("agent "+agent+" win at piece "+k+". Recursion="+recursion);
                        float[] t = {win, k};
                        return t;
                    } else if(win*agent>nextBest[0]){
                        //Common.prn("agent "+agent+" found tie at piece "+k+". Recursion="+recursion);
                        nextBest[0] = win;
                        nextBest[1] = k;
                    }
                    if(debug && recursion==0){
                        Common.prn("agent "+agent+" found win="+win+ " at piece "+k+". Recursion="+recursion);
                    }
                }
            }
        }
        //nextBest[0] *= agent; NO
        return nextBest;
    }
    //controls the looping of monte carlo search strands
    //for all best moves (that don't immediately tie or lose), call bestMove on mcLimit of them
    public float initHeurLoop(boolean pieceAlg, byte agent, GState s, byte piece, int recursion, boolean debug){
        //incCount2();
        if(pieceAlg){ //pick a piece
            //shuffle all pieces
            int[] res = s.randPieces();
            byte bestPieceOutcome = -1; //opp wins; 0 forced tie, 1 neutral, 2 next wins
            byte[] bestPieces = new byte[32]; //contains pieces to "call" bestMove on
            byte numBestPieces = 0;
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
                                
                                byte winH = h.win(ns); //outcome for next player
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
                        numBestPieces = 1;
                        bestPieces = new byte[32]; //erase
                        bestPieces[0] = (byte)res[k];
                    } else if(worstMoveOutcome==bestPieceOutcome){
                        bestPieces[numBestPieces] = (byte)res[k];
                        numBestPieces++;
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
                    Common.prn(s.toString());
                }
                return (float)(agent*-1);
            } else if(bestPieceOutcome==0){
                if (debug){
                    Common.prnYel("piece agents tied at recursion "+recursion);
                    Common.prn("piece "+bestPieces[0]);
                    Common.prn(s.toString());
                }
                return (float)0;
            } else{
                //MC time!
                float win = 0;
                int minMC = (numBestPieces < mcLimit) ? numBestPieces : mcLimit;
                int numRep = (numBestPieces < mcLimit) ? (mcLimit / numBestPieces) : 1;
                if (debug)
                    Common.prn("numMC="+minMC+"; numRep="+numRep);
                long maxTime = 0;
                long currTime = System.currentTimeMillis();
                for(int i=0; i<minMC && !timeout; i++){
                    //heurLoop(boolean pieceAlg, byte agent, GState s, byte piece, int recursion, boolean debug)
                    for(int j=0; j<numRep && !timeout; j++){ //ideally would ensure unique starting point at next
                        //level
                        //incCount();
                        win+=heurLoop(false, (byte)(-1*agent), s, bestPieces[i], 1+recursion, debug);
                        debug = false; //depth only

                        // long newTime = System.currentTimeMillis();
                        // if(newTime - currTime > maxTime){
                        //     maxTime = newTime - currTime;
                        // }
                        // currTime = newTime;
                        // long timeElapsed = newTime - timeStart;
                        if(System.currentTimeMillis() - timeStart > 9800){
                            timeout = true;
                            Common.prnRed("timeout!");
                        }
                    }
                }
                return win/(minMC*numRep);
            }
        } else{
            //for all moves, call bestPiece with each, unless only one move left or won
            int[] res = s.randMoves();
            byte bestMoveOutcome = -1; //-1 opp wins; 0 forced tie, 1 neutral, 2 I win
            GState[] bestMoves = new GState[25];
            int bestMove = -1;
            int numBestMoves = 0;

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
                            bestMoves[0] = ns;
                            bestMoveOutcome = 2;
                            bestMove = res[i*5+j];
                            exitMove = true;
                            numBestMoves = 1;
                        }else if(bestMoveOutcome<1 && winH ==0){
                            //neutral
                            bestMoves[0] = ns;
                            bestMoveOutcome =1;
                            numBestMoves = 1;
                        } else if(bestMoveOutcome<0 && winH == -1){
                            //tie better than loss
                            bestMoves[0] = ns;
                            bestMove = res[i*5+j];
                            bestMoveOutcome =0;
                            bestMoveOutcome =1;
                            numBestMoves = 1;
                        } else if(winH ==0){
                            //for iterating with MC
                            bestMoves[numBestMoves] = ns;
                            numBestMoves++;
                        }
                    }
                }
            }
            if(bestMoveOutcome == -1){
                if(debug){
                    Common.prnRed("move agent "+agent+" lost at recursion "+recursion);
                    Common.prn("move "+bestMove/5+", "+bestMove%5+", "+piece);
                    Common.prn(bestMoves[0].toString());
                }
                return (byte)(agent*-1);
            } else if(bestMoveOutcome==0){
                if (debug){
                    Common.prnRed("move agents tied at recursion "+recursion);
                    Common.prn("move "+bestMove/5+", "+bestMove%5+", "+piece);
                    Common.prn(bestMoves[0].toString());
                }
                return 0;
            } else{
                //MC time!
                float win = 0;
                int minMC = (numBestMoves < mcLimit) ? numBestMoves : mcLimit;
                int numRep = (numBestMoves < mcLimit) ? (mcLimit / numBestMoves) : 1;
                // long maxTime = 0;
                //long currTime = System.currentTimeMillis();
                for(int i=0; i<minMC && !timeout; i++){
                    //heurLoop(boolean pieceAlg, byte agent, GState s, byte piece, int recursion, boolean debug)
                    for(int j=0; j<numRep && !timeout; j++){ //ideally would ensure unique starting point at next
                        //level
                        //incCount();
                        win+=heurLoop(true, agent, bestMoves[i], (byte)-1, 1+recursion, debug);
                        debug = false; //depth only
                        
                        //long newTime = System.currentTimeMillis();
                        // if(newTime - currTime > maxTime){
                        //     maxTime = newTime - currTime;
                        // }
                        // currTime = newTime;
                        // long timeElapsed = newTime - timeStart;
                        if(System.currentTimeMillis() - timeStart > 9800){
                            timeout = true;
                            Common.prnRed("timeout!");
                            //Common.prnYel("currTime="+newTime+" start="+timeStart);
                        }
                    }
                }
                return win/(minMC*numRep);
            }
        }
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
                    //incCount();
                    return (byte)(agent*-1);
                } else if(bestPieceOutcome==0){
                    if (debug){
                        Common.prnYel("piece agents tied at recursion "+recursion);
                        Common.prn("piece "+bestPiece);
                        Common.prn(s.toString());
                    }
                    //incCount();
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
                    //incCount();
                    return (byte)(agent*-1);
                } else if(bestMoveOutcome==0){
                    if (debug){
                        Common.prnRed("move agents tied at recursion "+recursion);
                        Common.prn("move "+bestMovePos/5+", "+bestMovePos%5+", "+piece);
                        Common.prn(bestMove.toString());
                    }
                    //incCount();
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
    private final Object lock = new Object();
    int counter = 0;
    public int incCount() {
        synchronized (lock) {
            return ++counter;
        }
    }
    int counter2 = 0;
    public int incCount2() {
        synchronized (lock) {
            return ++counter2;
        }
    }
}
