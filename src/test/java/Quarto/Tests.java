package Quarto;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
//import static org.junit.Assert.assertTrue;

//import org.junit.Test;

public class Tests {
  /*
  java -classpath "/home/tye/Dropbox/cs4725/quarto/RobotMessiah/target/test-classes:/home/tye/Dropbox/cs4725/quarto/RobotMessiah/target/classes" Quarto.Tests
  Dec 1: multi threaded app with null board MC=4 took 2501438ms, returned a tie at piece 29
  multi threaded with rand no win 2, limit 3, MC=3 took 9097, returned 0.67 at piece 11
  same with 4MC took 15s, returned 0.5 at piece 0
  same with limit =2, MC=10 took 87ms, returned 0 at piece 31
  */
  public static void main(String[] args) throws InterruptedException {
   //assertTrue(test2() == 42);
   Common.startNanoTimer();
   test7varied();
   Common.prn(""+Common.getNanosecondsFromTimer()/1000000);
  //  Common.startNanoTimer();
  //  test13();
  //  Common.prn(""+Common.getNanosecondsFromTimer()/1000000);
  }
  private static int test7varied(){
    GameClient gc = new GameClient();
    //"src/test/java/Quarto/rand_no_win4_closer2"
    RobotMessiah rm = new RobotMessiah(gc, "src/test/java/Quarto/rand_no_win2");
    Common.prn(Arrays.toString(rm.bestPiece((byte)1, rm.currState, 0, 2, false)));
    Common.prn("init win status (1=win, 0=no win, -1 no possible win, -2 next player always wins)= "
    +rm.h.win(rm.currState));
    return 1;
  }
  private static void test13(){
    //heurLoop(boolean pieceAlg, byte agent, GState s, byte piece, int recursion, boolean debug)
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, null);
    int[] results = new int[3];
    for(int i=0;i<1000;i++){
     int res = rm.heurLoop(true, (byte)1, rm.currState, (byte)-1, 0, false);
     if (res==0){
       results[2]++;
     } else if(res==1){
       results[0]++;
     } else if (res==-1){
       results[1]++;
     }
    }
    Common.prnRed(""+Arrays.toString(results));
  }
  private static int test12() throws InterruptedException {
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, null);
    //Common.prn(Arrays.toString(rm.bestPiece((byte)1, rm.currState, 0, 1, false)));
    //Common.prn("init win status (1=win, 0=no win, -1 no possible win, -2 next player always wins)= "
    //+rm.h.win(rm.currState));
    int MC_LIMIT = 4;
    byte agent = 1;

    AsyncSearch t[] = new AsyncSearch[MC_LIMIT];
    for(int k=0; k<3; k++){
        //RobotMessiah rm, boolean pieceAgent, byte agent, GState ns, byte piece, int recursion, boolean debug
        t[k] = new AsyncSearch(rm, false, agent, rm.currState, (byte)0, 0, false);
        new Thread(t[k]).start();
        //win+= heurRandPiece(agent, ns, recursion+1, debug); //chose different pieces?
    }
    // AsyncSearch t = new AsyncSearch(rm, false, agent, rm.currState, (byte)0, 0, false);
    //new Thread(t).start();
    t[MC_LIMIT-1] = new AsyncSearch(rm, false, agent, rm.currState, (byte)0, 0, false);
    t[MC_LIMIT-1].run(); //give main thread something to do
    float win = t[MC_LIMIT-1].getWin();
    //int win=0;
    //t.join();
    //Common.prn("thread="+t.win);
    //win+=t.win;
    for(int k=0; k<3; k++){
        t[k].join();
        Common.prn("thread="+t[k].getWin());
        win+=t[k].win;
    }
    Common.prn("thread="+t[3].getWin());
    return 1;
  }
  private static int testLogicHeur(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "src/test/java/Quarto/moveagentloseswithpiece2");
    Common.prn("init win status (1=win, 0=no win, -1 no possible win, -2 next player always wins)= "
    +rm.h.win(rm.currState));
    return 1;
  }
  private static int test11() throws InterruptedException {
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, null);
    Common.prn(Arrays.toString(rm.bestPiece((byte)1, rm.currState, 0, 1, false)));
    Common.prn("init win status (1=win, 0=no win, -1 no possible win, -2 next player always wins)= "
    +rm.h.win(rm.currState));
    Common.prn("counter="+rm.counter);
    return 1;
  }
  private static void test10(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "src/test/java/Quarto/pieceagentlost2-0&1samechar");
    Common.prn(""+rm.h.win(rm.currState));
  }
  private static void test9(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, null);
    int[] results = new int[3];
    for(int i=0;i<1000;i++){
     int res = rm.heurRandPiece((byte)1, rm.currState, 0, false);
     if (res==0){
       results[2]++;
     } else if(res==1){
       results[0]++;
     } else if (res==-1){
       results[1]++;
     }
    }
    Common.prnRed(""+Arrays.toString(results));
  }
  private static int test8(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, null);
    Common.prn(""+rm.heurRandPiece((byte)1, rm.currState, 0, false));
    return 1;
  }
  private static int test7(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "src/test/java/Quarto/rand_no_win4_closer2");
    Common.prn(Arrays.toString(rm.bestPiece((byte)1, rm.currState, 0, 3, false)));
    Common.prn("init win status (1=win, 0=no win, -1 no possible win, -2 next player always wins)= "
    +rm.h.win(rm.currState));
    return 1;
  }
  private static int test6(){
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "src/test/java/Quarto/rand_no_win4");
    Common.prn(rm.currState.toString());
    return 1;
  }
  // private static int test5(){
  //   GameClient gc = new GameClient();
  //   RobotMessiah rm = new RobotMessiah(gc, null);
  //   GState rs = rm.currState.randState();
  //   Common.prn(rs.toString());
  //   Common.prn(""+rm.win(rs));
  //   return 1;
  // }
  /*
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
  }*/
/*
  private static int test2(){
    //QuartoBoard qboard = new QuartoBoard(5, 5, 32, "near_win_state_diagonal");
    GameClient gc = new GameClient();
    RobotMessiah rm = new RobotMessiah(gc, "near_win_state_diagonal");
    return rm.heuristic(rm.quartoBoard.board);
  }*/
  // private static int test3(){
  //   GameClient gc = new GameClient();
  //   RobotMessiah rm = new RobotMessiah(gc, "src/test/java/Quarto/near_win_state_diagonal");
  //   // try{
  //   //   FileWriter fw = new FileWriter("test3");
  //   //   fw.write(rm.currState.toString());
  //   //   RobotMessiah.state s = rm.currState.copy();
  //   //   fw.append(s.toString());
  //   //   fw.close();
  //   // } catch(Exception e){}
  //   Common.prn(rm.currState.toString());
  //   RobotMessiah.state s = rm.currState.copy();
  //   Common.prn(s.toString());
  //   return 1;
  // }
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
