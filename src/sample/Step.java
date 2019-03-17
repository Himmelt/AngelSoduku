package sample;

import java.util.Set;

public class Step {
    public int row;
    public int col;
    public int num;
    public Set<Integer> possibles;

    public Step(int row, int col, int num, Set<Integer> possibles) {
        this.row = row;
        this.col = col;
        this.num = num;
        this.possibles = possibles;
    }

    public String toString() {
        return "Row:" + row + ",Col:" + col + ",Num:" + num;
    }
}
