
import java.util.Random;
public class GState{
    Random rand = new Random();
    //x, then y, then characteristics (as a number), -1 for null
    byte[][] board = new byte[5][5];
    //first dimensions are characteristics as binary number (0 to 2^5-1), last is x y coords
    byte[][] pieces = new byte[32][2];
    //byte numPlayed = 0;
    //byte[][] charRemain = {{16,16,16,16,16},{16,16,16,16,16}}; //0 is pieces w/ 0, 1 is p w/ 1
    //chars not played
    public GState(){
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
    public GState copy(){
        GState temp = new GState();
        for(byte i=0; i<5;i++){
            System.arraycopy(board[i], 0, temp.board[i], 0, 5);
        }
        for(byte i=0; i<32; i++){
            System.arraycopy(pieces[i], 0, temp.pieces[i], 0, 2);
        }
        //temp.numPlayed = numPlayed;
        return temp;
    }
    public GState randState(int numEmpty){
        GState s = new GState();
        
        int[] pieces = randPieces();
        int[] spaces = randMoves();
        for(byte j=0;j<25-numEmpty; j++){
            s.board[spaces[j]/5][spaces[j]%5] = (byte)pieces[j];
            s.pieces[j][0]=(byte)(spaces[j]/5);
            s.pieces[j][1]=(byte)(spaces[j]%5);
        }
        return s;
    }
    //from https://stackoverflow.com/questions/7404666/generating-random-unique-sequences-in-java
    public int[] randPieces(){
        int[] res = new int[32];
        for (int i = 0; i < 32; i++) {
            int d = rand.nextInt(i+1);
            res[i] = res[d];
            res[d] = i;
        }
        return res;
    }
    public int[] randMoves(){ 
        int[] res2 = new int[25];
        for (int i = 0; i < 25; i++) {
            int d = rand.nextInt(i+1);
            res2[i] = res2[d];
            res2[d] = i;
        }
        return res2;
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