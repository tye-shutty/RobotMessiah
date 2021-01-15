
public class LogicHeuristic extends Heuristic{
    
    public byte win(GState s){
        //1 for win, 0 for no win, -1 for no possible win, -2 for next player always wins
        //logically assesses the current state for win conditions
        byte[][] wins = new byte[2][5]; //0= char has no wins, 1= 1 has win, represents winning
        //chars, first row is 0s, 2nd is 1s
        byte nextWin = 0;
        byte notPossible = -1;
        boolean pivot = false;

        for(byte i=0; i<5; i++){
            byte[] currChars = {0,0,0,0,0};
            byte nullCount = 0;
            byte nullPos = -1;
            //fill up currChars array
            for(byte j=0; j<5; j++){
                byte rc = Common.pivotLookup(s.board, pivot,i,j);
                if(rc == -1){
                    nullCount++;
                    nullPos=j;
                } else{
                    for(byte z=0;z<5;z++){
                        currChars[4-z] += 1 & (byte)(rc >> z); //0 pos is most significant
                    }
                }
            }
            //check for immediate win
            if(nullCount == 0){
                for(byte j=0; j<5; j++){
                    if(currChars[j] == 0 || currChars[j] == 5){
                        return 1;
                    }
                }
            //check if next player wins
            } else if(nextWin != -2 && nullCount==1){
                for(byte j=0;j<5;j++){
                    if(currChars[j]==4){ //1s will win
                        if(wins[0][j]> 0){ //0s also win
                            nextWin = -2;
                        }else{
                            wins[1][j]++; //1s will win
                        }
                    } else if(currChars[j]==0){ //0s will win
                        if(wins[1][j]> 0){ //1s also win
                            nextWin = -2;
                        } else{
                            wins[0][j]++;
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
                for(byte j=0;j<5;j++){
                    if(currChars[j]==4){ //1s will win
                        if(wins[0][j]> 0){ //0s also win
                            nextWin = -2;
                        }else{
                            wins[1][j]++; //1s will win
                        }
                    } else if(currChars[j]==0){ //0s will win
                        if(wins[1][j]> 0){ //1s also win
                            nextWin = -2;
                        } else{
                            wins[0][j]++;
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
}
