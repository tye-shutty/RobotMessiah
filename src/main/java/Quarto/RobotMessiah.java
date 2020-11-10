//Author: Tye Shutty
//Adapted from Michael Flemming's code
package Quarto;
import java.io.BufferedReader;
import java.io.FileReader;

//maybe override choosePieceTurn(), etc to make more usable arrays 
public class RobotMessiah extends QuartoAgent{

    private class state{
        int[][][] board = new int[5][5][5];
        //first 5 dimensions are characteristics, last is x y coords
        int[][][][][][] playedPieces = new int[2][2][2][2][2][2];
        int piecesPlayed = 0;
    }
    private state currState = new state();

    public RobotMessiah(GameClient gameClient, String stateFileName){
        super(gameClient, stateFileName);
        currState = new state();
        //empty board
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                for(int k=0;k<5;k++){
                    currState.board[i][j][k] = -1;
                }
            }
        }
        
		if(stateFileName != null){
            try{
                BufferedReader br = new BufferedReader(new FileReader(stateFileName));
                String line;
                int row = 0;
                
                while((line = br.readLine()) != null && row < 5){
                    String[] splitted = line.split("\\s+");
                    for(int col=0; col<5; col++){
    
                        if(!splitted[col].equals("null")){
                            for(int i=0;i<5;i++){
                                if(splitted[col].charAt(i) == '0'){
                                    currState.board[row][col][i] = 0;
                                }
                            }
                            currState.playedPieces[currState.board[row][col][0]]
                            [currState.board[row][col][1]]
                            [currState.board[row][col][2]]
                            [currState.board[row][col][3]]
                            [currState.board[row][col][4]][0] = row;
                            currState.playedPieces[currState.board[row][col][0]]
                            [currState.board[row][col][1]]
                            [currState.board[row][col][2]]
                            [currState.board[row][col][3]]
                            [currState.board[row][col][4]][1] = col;

                            currState.piecesPlayed++;
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
        //start the server
        GameClient gameClient = new GameClient();

        String ip = null;
        String stateFileName = null;
        //IP must be specified
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
        if(empty(board)){
            //return random piece
        }
        this.startTimer();
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

    private QuartoPiece pivotLookup(QuartoPiece[][] board, boolean pivot, int i, int j){
        if(pivot){
            return board[j][i];
        }
        return board[i][j];
    }
    public int bestPiece(int maxAgent, state s){
        //for all pieces, call bestMove with each
        //state is 3D vector, where x is x axis, y is y axis, and z is characteristics,
        //if no piece played, all characteristics will be -1
        
    }
    //returns -1 for min win, 1 for max win, 0 for tie
    public int bestMove(int maxAgent, state s){
        //for all moves, call bestPiece with each, unless only one move left or won
        int win = winOpenings(this.quartoBoard.board);
        if(s.piecesPlayed == 31){
            //could be more efficient
            return win > 0 ? maxAgent : 0;
        } else if(win == 0){
            //check if win possible
            return 0
        } else if(){
            //check if won

            return win > 0 ? maxAgent : 0;
        } else{
            //copy game state, add piece to each null position
            
            //chose best outcome if max, worst if min
            while(bestPiece(-1*maxAgent)*maxAgent != 1)
            Boolean result = bestPiece(-1*maxAgent);
            
        }
    }
    /**public state initializeState(){
        state s = new state();
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                for(int k=0; k<5; k++){
                    if(this.quartoBoard.board[i][j] != null){
                        s.board[i][j][k] = this.quartoBoard.board[i][j].getCharacteristicsArray()[k] ? 1 : 0;
                    } else{
                        s.board[i][j][k] = -1;
                    }
                }

            }
        }
        
    }**/
    //in future, consider machine learning to find ideal value for heuristic
    //returns sum of all win possibilities, ignorant of remaining pieces
    public int winOpenings(QuartoPiece[][] state){
        int winPossibilities = 0; //1 possibility for every characteristic quintuple
        this.startNanoTimer();

        //check rows and columns for win possibility(excludes existing peices)
        boolean pivot = false;
        for(int i=0; i<5; i++){
            int nullCount = 0;
            int[] currChars ={0,0,0,0,0};
            for(int j=0; j<5; j++){
                if(pivotLookup(state, pivot,i,j) == null){
                    nullCount++;
                } else{
                    boolean[] rc = pivotLookup(state, pivot,i,j).getCharacteristicsArray();
                    for(int z=0;z<5;z++){
                        currChars[z] += rc[z] ? 1 : 0;
                    }
                }
            }
            for(int j=0; j<5; j++){
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
                if(this.quartoBoard.board[j][i[0]+i[1]*j] == null){
                    nullCount++;
                } else{
                    boolean[] rc = this.quartoBoard.board[j][i[0]+i[1]*j].getCharacteristicsArray();
                    for(int z=0;z<5;z++){
                        currChars[z] += rc[z] ? 1 : 0;
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
    
    @Override
    protected String moveSelectionAlgorithm(int pieceID){

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
        }

        // If no winning move is found in the above code, then return a random(unoccupied) square
        int[] move = new int[2];
        QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
        move = copyBoard.chooseRandomPositionNotPlayed(100);

        Common.prnRed("random solution: "+this.getNanosecondsFromTimer()+"ns");
        return move[0] + "," + move[1];
    }
	//Records the current time in nanoseconds from when this function is called
	protected void startNanoTimer(){
		startTime = System.nanoTime();
	}

	//gets the time difference between now and when startNanoTimer() was last called
	protected long getNanosecondsFromTimer(){
		return System.nanoTime() - startTime;
    }
}
