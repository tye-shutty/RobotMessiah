package Quarto;
//Author: Tye Shutty
public class Common {

  public static String red = "\u001B[31m";
  public static String white = "\u001B[37m";

  public static void prn(String s){
    System.out.println(s);
  }
  public static void prnRed(String s){
    System.out.println(red+s+white);
  }
  public static void pr(String s){
    System.out.print(s);
  }
}
