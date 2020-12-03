//Author: Tye Shutty
import java.io.FileWriter;
public class Common {

  public static String red = "\u001B[31m";
  public static String yellow = "\u001B[33m";
  public static String white = "\u001B[37m";

  public static void prn(String s){
    System.out.println(s);
  }
  public static void prnRed(String s){
    System.out.println(red+s+white);
  }
  public static void prnYel(String s){
    System.out.println(yellow+s+white);
  }
  public static void pr(String s){
    System.out.print(s);
  }
  public static void out(String file, String content, int append){
    try{
      FileWriter fw = new FileWriter("test3");
      if(append==1){
        fw.append(content);
      } else{
        fw.write(content);
      }
      fw.close();
    } catch(Exception e){
      System.out.println(e);
    }
  }
  //Records the current time in nanoseconds from when this function is called
  static long startTime;
	public static void startNanoTimer(){
		startTime = System.nanoTime();
	}

	//gets the time difference between now and when startNanoTimer() was last called
	public static long getNanosecondsFromTimer(){
		return System.nanoTime() - startTime;
    }
  public static byte pivotLookup(byte[][] board, boolean pivot, byte i, byte j){
      if(pivot){
          return board[j][i];
      }
      return board[i][j];
  }
}
