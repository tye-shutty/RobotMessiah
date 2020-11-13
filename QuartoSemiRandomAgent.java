//Author: Tye Shutty
//Adapted from Michael Flemming's code
package Quarto;

public class QuartoSemiRandomAgent extends QuartoAgent {

    //Example AI
    public QuartoSemiRandomAgent(GameClient gameClient, String stateFileName) {
        // because super calls one of the super class constructors(you can overload constructors), you need to pass the parameters required.
        super(gameClient, stateFileName);
    }

    //MAIN METHOD
    public static void main(String[] args) {
        //start the server
        GameClient gameClient = new GameClient();

        String ip = null;
        String stateFileName = null;
        //IP must be specified
        if(args.length > 0) {
            ip = args[0];
        } else {
            System.out.println("No IP Specified");
            System.exit(0);
        }
        if (args.length > 1) {
            stateFileName = args[1];
        }

        gameClient.connectToServer(ip, 4321);
        QuartoSemiRandomAgent quartoAgent = new QuartoSemiRandomAgent(gameClient, stateFileName);
        quartoAgent.play();

        gameClient.closeConnection();

    }


    /*
	 * This code will try to find a piece that the other player can't use to win immediately
	 */
    @Override
    protected String pieceSelectionAlgorithm() {
        //some useful lines:
        //String BinaryString = String.format("%5s", Integer.toBinaryString(pieceID)).replace(' ', '0');

        this.startTimer();
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
                //handle for when we are over some imposed time limit (make sure you account for communication delay)
            }
            String message = null;
            //for every other i, check if there is a missed message
            /*
            if (i % 2 == 0 && ((message = this.checkForMissedServerMessages()) != null)) {
                //the oldest missed message is stored in the variable message.
                //You can see if any more missed messages are in the socket by running this.checkForMissedServerMessages() again
            }
            */
        }


        //if we don't find a piece in the above code just grab the first random piece
        int pieceId = this.quartoBoard.chooseRandomPieceNotPlayed(100);
        String BinaryString = String.format("%5s", Integer.toBinaryString(pieceId)).replace(' ', '0');


        return BinaryString;
    }

    /*
     * Do Your work here
     * The server expects a move in the form of:   row,column
     */
    private QuartoPiece pivotLookup(boolean pivot, int i, int j){
        if(pivot){
            return this.quartoBoard.board[j][i];
        }
        return this.quartoBoard.board[i][j];
    }
    @Override
    protected String moveSelectionAlgorithm(int pieceID) {

        //If there is a winning move, take it
        // [This is where you should insert the required code for Assignment 1.]

        //NOTE TO MARKER
        //I added code to other files (QuartoAgent.java, Common.java), such as a nano timer and printing in red.
        //Instead of reusing Prof Flemming's methods, I think my own purpose-written code would be faster.
        //He emailed me "I'm not concerned about speed or about the length of the code"
        //In class we were told that speed is essential for the final project.
        //Speed can be improved at the expense of code length.
        //Furthermore, a dedicated method will allow me to tweak it over time, in preparation for the project. 
        this.startNanoTimer();
        QuartoPiece curr = this.quartoBoard.getPiece(pieceID);
        int[] chars = {0,0,0,0,0};
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
                if(pivotLookup(pivot,i,j) == null){
                    nullCount++;
                    nullj = j;
                    if(nullCount > 1){
                        break;
                    }
                } else{
                    boolean[] rc = pivotLookup(pivot,i,j).getCharacteristicsArray();
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

        // If no winning move is found in the above code, then return a random (unoccupied) square
        int[] move = new int[2];
        QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
        move = copyBoard.chooseRandomPositionNotPlayed(100);

        Common.prnRed("random solution: "+this.getNanosecondsFromTimer()+"ns");
        return move[0] + "," + move[1];
    }



    //loop through board and see if the game is in a won state
    private boolean checkIfGameIsWon() {

        //loop through rows
        for(int i = 0; i < NUMBER_OF_ROWS; i++) {
            //gameIsWon = this.quartoBoard.checkRow(i);
            if (this.quartoBoard.checkRow(i)) {
                System.out.println("Win via row: " + (i) + " (zero-indexed)");
                return true;
            }

        }
        //loop through columns
        for(int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            //gameIsWon = this.quartoBoard.checkColumn(i);
            if (this.quartoBoard.checkColumn(i)) {
                System.out.println("Win via column: " + (i) + " (zero-indexed)");
                return true;
            }

        }

        //check Diagonals
        if (this.quartoBoard.checkDiagonals()) {
            System.out.println("Win via diagonal");
            return true;
        }

        return false;
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
