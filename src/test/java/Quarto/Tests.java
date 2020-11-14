package Quarto;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
//import static org.junit.Assert.assertTrue;

//import org.junit.Test;

public class Tests {
  public static void main(String[] args){
   //assertTrue(test2() == 42);
   test5();
  }
  private static int test6(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "src/test/java/Quarto/rand_no_win1");
    Common.prn(rm.currState.toString());
    return 1;
  }
  private static int test5(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, null);
    RobotMessiah.state rs = rm.currState.randState();
    Common.prn(rs.toString());
    Common.prn(""+rm.win(rs));
    return 1;
  }
  private static int test4(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, null);
    // try{
    //   FileWriter fw = new FileWriter("test3");
    //   fw.write(rm.currState.toString());
    //   RobotMessiah.state s = rm.currState.copy();
    //   fw.append(s.toString());
    //   fw.close();
    // } catch(Exception e){}
    rm.bestPiece((byte)1, rm.currState);
    return 1;
  }
/*
  private static int test2(){
    //QuartoBoard qboard = new QuartoBoard(5, 5, 32, "near_win_state_diagonal");
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "near_win_state_diagonal");
    return rm.heuristic(rm.quartoBoard.board);
  }*/
  private static int test3(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "src/test/java/Quarto/near_win_state_diagonal");
    // try{
    //   FileWriter fw = new FileWriter("test3");
    //   fw.write(rm.currState.toString());
    //   RobotMessiah.state s = rm.currState.copy();
    //   fw.append(s.toString());
    //   fw.close();
    // } catch(Exception e){}
    Common.prn(rm.currState.toString());
    RobotMessiah.state s = rm.currState.copy();
    Common.prn(s.toString());
    return 1;
  }
/*
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
  }*/
}
