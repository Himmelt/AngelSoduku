package sample;

import java.util.List;

public class Step {
    public int row;
    public int col;
    public int num;
    public List<Integer> possibles;

    public Step(int row, int col, int num, List<Integer> possibles) {
        this.row = row;
        this.col = col;
        this.num = num;
        this.possibles = possibles;
    }

    public String toString() {
        return "Row:" + row + ",Col:" + col + ",Num:" + num;
    }
}
