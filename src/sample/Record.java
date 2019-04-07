package sample;

import java.util.Stack;

public class Record {
    private int record = 0;
    private final Stack<Integer> stack = new Stack<>();

    public void add(int delta) {
        record += delta;
    }

    public int getRecord() {
        return record;
    }

    public void multiPut(int best) {
        stack.push(best);
    }

    public Stack<Integer> getStack() {
        return stack;
    }
}
