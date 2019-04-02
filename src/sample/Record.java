package sample;

public class Record {
    private int record = 0;

    public void add(int delta) {
        record += delta;
    }

    public int getRecord() {
        return record;
    }
}
