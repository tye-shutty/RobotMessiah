package Quarto;

public abstract class Heuristic {
    //1 for win, 0 for no win, -1 for no possible win, -2 for next player always wins
    public abstract byte win(state s);
}
