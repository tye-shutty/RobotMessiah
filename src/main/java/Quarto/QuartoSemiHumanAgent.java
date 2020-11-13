package Quarto;
//Author: Tye Shutty
//Adapted from Michael Flemming's code

import java.util.Scanner;

public class QuartoSemiHumanAgent extends QuartoAgent {


    public QuartoSemiHumanAgent(GameClient gameClient, String stateFileName) {
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
        QuartoSemiHumanAgent quartoAgent = new QuartoSemiHumanAgent(gameClient, stateFileName);
        quartoAgent.play();

        gameClient.closeConnection();

    }


    /*
	 * Do Your work here
	 * The server expects a binary string, e.g.   10011
	 */
    @Override
    protected String pieceSelectionAlgorithm() {
        Common.prn("Select a piece:");
        this.quartoBoard.printBoardState();
        int c = 0;
        for(int i=0; i<32; i++){
            QuartoPiece p = this.quartoBoard.pieces[i];
            if(!p.isInPlay()){
                c++;
                Common.pr(p.binaryStringRepresentation() + "  ");
            }
            if(c==7){
                c=0;
                Common.prn("");
            }
        }
        Common.prn("");
        Scanner reader = new Scanner(System.in);
        String choice = reader.next();
        Common.prn("choice: "+choice);
        reader.nextLine();
        //reader.close();
        
        return choice;
    }

    /*
     * Do Your work here
     * The server expects a move in the form of:   row,column
     */
    @Override
    protected String moveSelectionAlgorithm(int pieceID) {
        //do work

        int[] move = new int[2];
        QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
        move = copyBoard.chooseRandomPositionNotPlayed(100);

        return move[0] + "," + move[1];
    }

}
