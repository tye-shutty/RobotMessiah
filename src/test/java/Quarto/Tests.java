package Quarto;
import java.util.Scanner;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Tests {
  public static void main(String[] args){
    assertTrue(test2() == 42);
  }

  private static int test2(){
    //QuartoBoard qboard = new QuartoBoard(5, 5, 32, "near_win_state_diagonal");
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "near_win_state_diagonal");
    return rm.heuristic(rm.quartoBoard.board);
  }

  private static void test1(){
    Common.prn("Test 1");
    GameClient emptyClient = new GameClient();
    GameServer emptyGameServer = new GameServer();
    QuartoServer server = new QuartoServer(emptyGameServer, "near_win_state_diagonal");
    QuartoSemiRandomAgent sr = new QuartoSemiRandomAgent(emptyClient,"near_win_state_diagonal");
    server.quartoBoard.printBoardState();

    Common.prn("Select a piece:");
    int c = 0;
    for(int i=0; i<32; i++){
        QuartoPiece p = server.quartoBoard.pieces[i];
        if(!p.isInPlay()){
            c++;
            Common.pr(p.binaryStringRepresentation() + ":"+p.getPieceID()+"  ");
        }
        if(c==5){
            c=0;
            Common.prn("");
        }
    }
    Common.prn("");
    Scanner reader = new Scanner(System.in);
    int choice = reader.nextInt();
    Common.prn("choice: "+choice);
    reader.nextLine();

    Common.prn(sr.moveSelectionAlgorithm(choice));
    Common.prn("end Test 1");
  }
}
