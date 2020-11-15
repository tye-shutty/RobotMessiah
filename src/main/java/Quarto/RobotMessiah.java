package Quarto;
//Author: Tye Shutty
//Adapted from Michael Flemming's code
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Random;

//maybe override choosePieceTurn(), etc to make more usable arrays 
public class RobotMessiah extends QuartoAgent{

    public class state{
        //x, then y, then characteristics (as a number), -1 for null
        byte[][] board = new byte[5][5];
        //first dimensions are characteristics as binary number (0 to 2^5-1), last is x y coords
        byte[][] pieces = new byte[32][2];
        //byte numPlayed = 0;
        //byte[][] charRemain = {{16,16,16,16,16},{16,16,16,16,16}}; //0 is pieces w/ 0, 1 is p w/ 1
        //chars not played
        public state(){
            for(int i=0;i<5;i++){
                for(int j=0;j<5;j++){
                    board[i][j]=-1;
                }
            }
            for(int i=0; i<32; i++){
                pieces[i][0]=-1;
                pieces[i][1]=-1;
            }
        }
        public state copy(){
            state temp = new state();
            for(byte i=0; i<5;i++){
                System.arraycopy(board[i], 0, temp.board[i], 0, 5);
            }
            for(byte i=0; i<32; i++){
                System.arraycopy(pieces[i], 0, temp.pieces[i], 0, 2);
            }
            //temp.numPlayed = numPlayed;
            return temp;
        }
        public state randState(){
            state s = new state();
            Random rand = new Random();
            //from https://stackoverflow.com/questions/7404666/generating-random-unique-sequences-in-java
            int[] res = new int[32];
            for (int i = 0; i < 32; i++) {
                int d = rand.nextInt(i+1);
                res[i] = res[d];
                res[d] = i;
            }
            for(byte j=0;j<5;j++){
                for(byte k=0;k<5;k++){
                    if(rand.nextInt(3)>0){
                        s.board[j][k]=(byte)res[j*5+k];
                        s.pieces[res[j*5+k]][0] = j;
                        s.pieces[res[j*5+k]][1] = k;
                    }
                }
            }
            return s;
        }
        public String toString(){
            String ret = "";
            for(byte i=0; i<5; i++){
                for (byte j=0; j<5; j++){
                    if(board[i][j] != -1){
                        String st = Integer.toBinaryString(board[i][j]);
                        while(st.length()<5){
                            st="0"+st;
                        }
                        ret+= st+"\t";
                    }
                    else{
                        ret+= "null\t";
                    }
                }
                ret+="\n";
            }
            ret+="\n[";
            for (byte j=0; j<32; j++){
                if(pieces[j][0] == -1){
                    String st = Integer.toBinaryString(j);
                    while(st.length()<5){
                        st="0"+st;
                    }
                    ret+= String.format("%6s", st);
                }
            }
            return ret+"]";
        }
    }
    public state currState = new state();

    public RobotMessiah(GameClient gameClient, String stateFileName){
        super(gameClient, stateFileName); //does error checks
        currState = new state();
        
		if(stateFileName != null){
            try{
                BufferedReader br = new BufferedReader(new FileReader(stateFileName));
                String line;
                byte row = 0;
                
                while((line = br.readLine()) != null && row < 5){
                    String[] splitted = line.split("\\s+");
                    for(byte col=0; col<5; col++){
    
                        if(!splitted[col].equals("null")){
                            currState.board[row][col] = 0; //-1 -> 0
                            for(byte i=0;i<5;i++){

                                if(splitted[col].charAt(4-i) == '1'){
                                    currState.board[row][col] = (byte)(currState.board[row][col] | (1 << i));
                                }
                            }
                            currState.pieces[Integer.parseInt(splitted[col], 2)][0] = row;
                            currState.pieces[Integer.parseInt(splitted[col], 2)][1] = col;
                            //currState.numPlayed++;
                        }
                    }
                    row++;
                }
                br.close();
                
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("RM Error parsing quarto File");
            }
		}
    }
    public static void main(String[] args){
        GameClient gameClient = new GameClient();
        String ip = null;
        String stateFileName = null;
        if(args.length > 0){
            ip = args[0];
        } else{
            System.out.println("No IP Specified");
            System.exit(0);
        }
        if(args.length > 1){
            stateFileName = args[1];
        }
        gameClient.connectToServer(ip, 4321);
        RobotMessiah quartoAgent = new RobotMessiah(gameClient, stateFileName);
        quartoAgent.play();
        gameClient.closeConnection();
    }
    @Override
    protected String pieceSelectionAlgorithm(){
        //if(currState.numPlayed == 0){
            //return random piece
        //}
        //this.startTimer();
        boolean skip = false;
        for(int i = 0; i < this.quartoBoard.getNumberOfPieces(); i++){
            skip = false;
            if(!this.quartoBoard.isPieceOnBoard(i)){
                for(int row = 0; row < this.quartoBoard.getNumberOfRows(); row++){
                    for(int col = 0; col < this.quartoBoard.getNumberOfColumns(); col++){
                        if(!this.quartoBoard.isSpaceTaken(row, col)){
                            QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
                            copyBoard.insertPieceOnBoard(row, col, i);
                            if(copyBoard.checkRow(row) || copyBoard.checkColumn(col) || 
                            copyBoard.checkDiagonals())
                            {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if(skip){
                        break;
                    }
                }
                if(!skip){
                    return String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0');
                }
            }
            if(this.getMillisecondsFromTimer() >(this.timeLimitForResponse - COMMUNICATION_DELAY)){
                //handle for when we are over some imposed time limit(make sure you account for communication delay)
            }
            String message = null;
        }
        //if we don't find a piece in the above code just grab the first random piece
        int pieceId = this.quartoBoard.chooseRandomPieceNotPlayed(100);
        String BinaryString = String.format("%5s", Integer.toBinaryString(pieceId)).replace(' ', '0');

        return BinaryString;
    }

    private byte pivotLookup(byte[][] board, boolean pivot, byte i, byte j){
        if(pivot){
            return board[j][i];
        }
        return board[i][j];
    }
    //0 is -1 for min win, 1 for max win, 0 for tie
    //agent is -1 if min agent, 1 if max
    //blind because it doesn't return the best move, just if it reaches it or not
    public byte blindBestMove(byte agent, state s, byte piece, int recursion){
        //for all moves, call bestPiece with each, unless only one move left or won
        byte tiePossible = (byte)(agent*-1); //opponent wins
        //for all moves (null spots), call bestPiece
        for(byte i=0; i<5;i++){
            for(byte j=0; j<5; j++){

                if(s.board[i][j] == -1){
                    //copy game state, add piece to null position
                    state ns = s.copy();
                    ns.board[i][j] = piece;
                    ns.pieces[piece][0]= i;
                    ns.pieces[piece][1]= j;
                    //ns.numPlayed++;
                    byte win = blindBestPiece(agent, ns, 1+recursion); //agent doesn't change
                    if(win == agent){
                        //Common.prnRed("agent "+agent+" win at move "+i+", "+j+". Recursion="+recursion);
                        return win;
                    } else if(tiePossible != 0 && win==0){
                        //Common.prn("agent "+agent+" found tie at move "+i+", "+j+". Recursion="+recursion);
                        tiePossible = 0;
                    } else{
                        //Common.prn("agent "+agent+" found nothing at move "+i+", "+j+". Recursion="+recursion);
                    }
                }
            }
            
        }
        return tiePossible;
    }
    //0 is min wins=-1, tie=0, max wins=1; 1 is piece resulting in best outcome, -1 if no piece.
    public byte[] bestPiece(byte agent, state s, int recursion){
        byte win = win(s);
        byte tiePossible[] = {(byte)(agent*-1),(byte)(agent*-1)}; //opponent wins
        if(win == 1){ //prev player has won
            Common.prnRed("agent "+agent+" lost at recursion="+recursion);
            byte[] t = {(byte)(agent*-1), (byte)-1};
            return t;
        } else if(win == -1){ //no win possible
            Common.prnRed("agent "+agent+" tied at recursion="+recursion);
            byte[] t = {0, (byte)-1};
            return t;
        } else if(win == -2){ //loss inescapable
            Common.prnRed("agent "+agent+" will lose at recursion="+recursion);
            byte[] t = {(byte)(agent*-1), (byte)-1};
            return t;
        } else{
            //for all unplayed pieces, call bestMove with each
            for(byte k=0; k< 32; k++){
                if(s.pieces[k][0] == -1){
                    win = blindBestMove((byte)(-1*agent), s, k, 1+recursion);
                    if(win == agent){
                        //Common.prnRed("agent "+agent+" win at piece "+k+". Recursion="+recursion);
                        byte[] t = {win, k};
                        return t;
                    } else if(tiePossible[0] != 0 && win==0){
                        //Common.prn("agent "+agent+" found tie at piece "+k+". Recursion="+recursion);
                        tiePossible[0] = 0;
                        tiePossible[1] = k;
                    } else{
                        //Common.prn("agent "+agent+" found nothing at piece "+k+". Recursion="+recursion);
                    }
                }
            }
        }
        return tiePossible;
    }
    //min wins=-1, tie=0, max wins=1
    public byte blindBestPiece(byte agent, state s, int recursion){
        byte win = win(s);
        byte tiePossible = (byte)(agent*-1); //opponent wins
        if(win == 1){ //prev player has won
            Common.prnRed("agent "+agent+" lost at recursion="+recursion);
            return (byte)(agent*-1);
        } else if(win == -1){ //no win possible
            Common.prnRed("agent "+agent+" tied at recursion="+recursion);
            return 0;
        } else if(win == -2){ //loss inescapable
            Common.prnRed("agent "+agent+" will lose at recursion="+recursion);
            return (byte)(agent*-1);
        } else{
            //for all unplayed pieces, call bestMove with each
            for(byte k=0; k< 32; k++){
                if(s.pieces[k][0] == -1){
                    win = blindBestMove((byte)(-1*agent), s, k, 1+recursion);
                    if(win == agent){
                        //Common.prnRed("agent "+agent+" win at piece "+k+". Recursion="+recursion);
                        return win;
                    } else if(tiePossible != 0 && win==0){
                        //Common.prn("agent "+agent+" found tie at piece "+k+". Recursion="+recursion);
                        tiePossible = 0;
                    } else{
                        //Common.prn("agent "+agent+" found nothing at piece "+k+". Recursion="+recursion);
                    }
                }
            }
        }
        return tiePossible;
    }
    //min wins=-1, tie=0, max wins=1
    public byte blindRandPiece(byte agent, state s, int recursion){
        byte win = win(s);
        byte tiePossible = (byte)(agent*-1); //opponent wins
        if(win == 1){ //prev player has won
            Common.prnRed("agent "+agent+" lost at recursion="+recursion);
            return (byte)(agent*-1);
        } else if(win == -1){ //no win possible
            Common.prnRed("agent "+agent+" tied at recursion="+recursion);
            return 0;
        } else if(win == -2){ //loss inescapable
            Common.prnRed("agent "+agent+" will lose at recursion="+recursion);
            return (byte)(agent*-1);
        } else{
            //for all unplayed pieces, call bestMove with each
            int[] res = new int[32];
            Random rand = new Random();
            for (int i = 0; i < 32; i++) {
                int d = rand.nextInt(i+1);
                res[i] = res[d];
                res[d] = i;
            }
            for(byte k=0; k< 32; k++){
                if(s.pieces[res[k]][0] == -1){
                    win = blindRandMove((byte)(-1*agent), s, (byte)res[k], 1+recursion);
                    if(win == agent){
                        //Common.prnRed("agent "+agent+" win at piece "+k+". Recursion="+recursion);
                        return win;
                    } else if(win==0){
                        //Common.prn("agent "+agent+" found tie at piece "+k+". Recursion="+recursion);
                        tiePossible = 0;
                    } //else{
                        //Common.prn("agent "+agent+" found nothing at piece "+k+". Recursion="+recursion);
                    //}
                    break;
                }
            }
        }
        return tiePossible;
    }
    public byte blindRandMove(byte agent, state s, byte piece, int recursion){
        //for all moves, call bestPiece with each, unless only one move left or won
        byte tiePossible = (byte)(agent*-1); //opponent wins
        
        int[] res = new int[25];
        Random rand = new Random();
        for (int i = 0; i < 25; i++) {
            int d = rand.nextInt(i+1);
            res[i] = res[d];
            res[d] = i;
        }
        boolean exit = false;
        for(byte i=0; i<5 & !exit;i++){
            for(byte j=0; j<5 & !exit; j++){
                if(s.board[res[i*5+j]/5][res[i*5+j]%5] == -1){
                    //copy game state, add piece to null position
                    state ns = s.copy();
                    ns.board[res[i*5+j]/5][res[i*5+j]%5] = piece;
                    ns.pieces[piece][0]= (byte)(res[i*5+j]/5);
                    ns.pieces[piece][1]= (byte)(res[i*5+j]%5);
                    //ns.numPlayed++;
                    byte win = blindRandPiece(agent, ns, 1+recursion); //agent doesn't change
                    if(win == agent){
                        //Common.prnRed("agent "+agent+" win at move "+i+", "+j+". Recursion="+recursion);
                        return win;
                    } else if(tiePossible != 0 && win==0){
                        //Common.prn("agent "+agent+" found tie at move "+i+", "+j+". Recursion="+recursion);
                        tiePossible = 0;
                    } //else{
                        //Common.prn("agent "+agent+" found nothing at move "+i+", "+j+". Recursion="+recursion);
                    //}
                    exit=true;
                }
            }
        }
        return tiePossible;
    }
    public byte win(state s){
        //1 for win, 0 for no win, -1 for no possible win, -2 for next player always wins

        byte[][][] wins = new byte[5][5][5]; //0= char has no wins, 1= 1 has win, 2 =0 has win
        byte nextWin = 0;
        byte notPossible = -1;
        boolean pivot = false;
        for(byte i=0; i<5; i++){
            byte[] currChars = {0,0,0,0,0};
            byte nullCount = 0;
            byte nullPos = -1;
            for(byte j=0; j<5; j++){
                byte rc = pivotLookup(s.board, pivot,i,j);
                if(rc == -1){
                    nullCount++;
                    nullPos=j;
                } else{
                    for(byte z=0;z<5;z++){
                        currChars[4-z] += 1 & (byte)(rc >> z); //0 pos is most significant
                    }
                }
            }
            if(nullCount == 0){
                for(byte j=0; j<5; j++){
                    if(currChars[j] == 0 || currChars[j] == 5){
                        return 1;
                    }
                }
            } else if(nextWin != -2 && nullCount==1){
                //add to wins
                byte x=pivot?nullPos:i;
                byte y=pivot?i:nullPos;
                for(byte j=0;j<5;j++){
                    if(currChars[j]==4){
                        if(wins[x][y][j]== 2){
                            nextWin = -2;
                        }else{
                            wins[x][y][j]= 1;
                        }
                    } else if(currChars[j]==0){
                        if(wins[x][y][j]== 1){
                            nextWin = -2;
                        } else{
                            wins[x][y][j]=2;
                        }
                    }
                }
            }
            if(notPossible != 0){
                for(byte j=0; j<5; j++){
                    //TODO consider chars remaining
                    // int charKind = currChars[j] > 0 ? 1 : 0;
                    // for(byte k=0; k<32;k++){
                    //     if (k>>(4-j)) | 1 
                    // }
                    // byte remain = 
                    if(currChars[j] == 0 || currChars[j]+nullCount == 5){
                        notPossible = 0;
                    }
                }
            }
            if(i==4 && pivot == false){
                i=-1;
                pivot=true;
            }
        }
        //check diagonals
        for(byte[] i={0,1}; i[1]>-3; i[0]=4, i[1]-=2){
            byte[] currChars ={0,0,0,0,0};
            byte nullCount = 0;
            byte nullPos = 0;
            for(byte j=0; j<5; j++){
                byte rc = s.board[j][i[0]+i[1]*j];
                if(rc == -1){
                    nullCount++;
                    nullPos = j;
                } else{
                    for(byte z=0;z<5;z++){
                        currChars[4-z] += 1 & (byte)(rc >> z);
                    }
                }
            }
            if(nullCount == 0){
                for(byte j=0; j<5; j++){
                    if(currChars[j] == 0 || currChars[j] == 5){
                        return 1;
                    }
                }
            } else if(nextWin != -2 && nullCount==1){
                //add to wins
                for(int j=0;j<5;j++){
                    if(currChars[j]==4){
                        if(wins[nullPos][i[0]+i[1]*nullPos][j]== 2){
                            nextWin = -2;
                        } else{
                            wins[nullPos][i[0]+i[1]*nullPos][j]= 1;
                        }
                    } else if(currChars[j]==0){
                        if(wins[nullPos][i[0]+i[1]*nullPos][j]== 1){
                            nextWin = -2;
                        } else{
                            wins[nullPos][i[0]+i[1]*nullPos][j]=2;
                        }
                    }
                }
            }
            if(notPossible != 0){
                for(byte j=0; j<5; j++){
                    if(currChars[j] == 0 || currChars[j]+nullCount == 5){
                        notPossible = 0;
                    }
                }
            }
        }
        //TODO check for nullCount>1 wins (lots of work for little payoff?)
        if(nextWin == -2){
            return -2;
        }
        return notPossible;
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
