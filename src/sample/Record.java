package sample;

import java.util.Stack;

public class Record {
    private int record = 0;
    private final Stack<Integer> stack = new Stack<>();
    private final Stack<Integer> selected = new Stack<>();

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

    public void setSelected(Stack<Integer> selected) {
        this.selected.clear();
        this.selected.addAll(selected);
    }

    public Stack<Integer> getSelected() {
        return selected;
    }
}
