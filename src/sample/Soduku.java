package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Soduku implements Initializable {

    private int[] layout = new int[4];
    private int[][] cells = new int[12][12];
    private Rectangle mask;
    private Rectangle nextMask;
    private Rectangle[][] masks = new Rectangle[4][4];
    private TextField[][] boxes = new TextField[12][12];
    private int[] rowQueue = new int[108];
    private int[] colQueue = new int[108];
    private final AtomicBoolean pause = new AtomicBoolean(true);
    private final Stack<Step> stacks = new Stack<>();

    public AnchorPane root;
    public TextField hard;
    public Button pause_button;
    public Label step_label;

    private static final double layoutX = 300;
    private static final double layoutY = 80;
    private static final double cellWidth = 40;
    private static final List<Integer> ONE2NINE = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

    public Soduku() {
        layout[0] = 0;
        layout[1] = 1;
        layout[2] = 2;
        layout[3] = 3;
        for (int i = 0; i < 12; i++) for (int j = 0; j < 12; j++) cells[i][j] = 0;
    }

    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Node> pane = root.getChildren();
        //root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        for (int i = 0; i < 13; i++) {
            Line line1 = new Line(layoutX, layoutY + cellWidth * i, layoutX + cellWidth * 12, layoutY + cellWidth * i);
            Line line2 = new Line(layoutX + cellWidth * i, layoutY, layoutX + cellWidth * i, layoutY + cellWidth * 12);
            if (i == 0 || i == 12) {
                line1.setStrokeWidth(3);
                line2.setStrokeWidth(3);
            } else if (i == 3 || i == 6 || i == 9) {
                line1.setStrokeWidth(1.5);
                line2.setStrokeWidth(1.5);
            } else {
                line1.setStrokeWidth(0.5);
                line2.setStrokeWidth(0.5);
            }
            pane.add(line1);
            pane.add(line2);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle rectangle = new Rectangle(cellWidth * 3 - 2, cellWidth * 3 - 2);
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setLayoutX(layoutX + cellWidth * 3 * j + 1);
                rectangle.setLayoutY(layoutY + cellWidth * 3 * i + 1);
                masks[i][j] = rectangle;
                pane.add(rectangle);
            }
        }
        mask = new Rectangle(cellWidth, cellWidth);
        mask.setFill(Color.TRANSPARENT);
        nextMask = new Rectangle(cellWidth, cellWidth);
        nextMask.setFill(Color.TRANSPARENT);
        pane.add(mask);
        pane.add(nextMask);
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                TextField text = new TextField();
                text.setPrefWidth(cellWidth);
                text.setPrefHeight(cellWidth);
                text.setAlignment(Pos.CENTER);
                text.setBackground(Background.EMPTY);
                text.setLayoutX(layoutX + cellWidth * j);
                text.setLayoutY(layoutY + cellWidth * i);
                text.setFont(Font.font(18));
                boxes[i][j] = text;
                pane.add(text);
            }
        }
    }

    public void updateLayoutUI() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                masks[i][j].setFill(layout[i] == j ? Color.LIGHTGRAY : Color.TRANSPARENT);
            }
        }
    }

    public void forwardStepUI(Step step) {
        if (step.num == 0) boxes[step.row][step.col].setText("");
        else boxes[step.row][step.col].setText(String.valueOf(step.num));
        //step_label.setText("Step:" + stacks.size() + "," + step.toString());
        mask.setFill(Color.rgb(0, 255, 0));
        mask.setLayoutX(layoutX + cellWidth * step.col);
        mask.setLayoutY(layoutY + cellWidth * step.row);
    }

    public void backwardStepUI(Step step) {
        boxes[step.row][step.col].setText("");
        //step_label.setText("Step:" + stacks.size() + "," + step.toString());
        mask.setFill(Color.rgb(255, 0, 0));
        mask.setLayoutX(layoutX + cellWidth * step.col);
        mask.setLayoutY(layoutY + cellWidth * step.row);
    }

    private void initCellUI(int row, int col) {
        boxes[row][col].setText("");
    }

    public void updateStepLabel(Step step) {
        step_label.setText(step.toString());
    }

    public void extractSoduku() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                try {
                    cells[i][j] = Byte.parseByte(boxes[i][j].getText());
                } catch (Throwable e) {
                    cells[i][j] = 0;
                }
            }
        }
    }

    public void checkTheSoduku() {
        System.out.println(checkSoduku());
    }

    public void generateSoduku() {
        String text = hard.getText();
        new Thread(() -> {
            if (text.matches("\\d+")) {
                generateSoduku(Integer.parseInt(text));
            } else generateSoduku(17);
        }).start();
        System.out.println("Running....");
    }

    public void goNextStep() {
        int next = stacks.size();
        int row = rowQueue[next];
        int col = colQueue[next];
        if (next == 0) {
            forward(row, col, 1, next + 1);
            return;
        }
        //Step step = new Step(row, col, 0, checkPossibles(row, col));
        Step currentStep = stacks.peek();
        boolean success = false;
        for (int num : currentStep.possibles) {
            // TODO 一定是可以的？因为上一步已经计算可能性了
            if (canPlace(row, col, num)) {
                forward(row, col, num, next + 1);
                success = true;
                break;
            }
        }
        if (!success) {
            //updateNextStepUI(rowQueue[next + 1], colQueue[next + 1]);
            backward(row, col);
        }
    }

    private ArrayList<Integer> checkRandPossibles(int row, int col) {
        ArrayList<Integer> list = getRandQueue(1, 9);
        for (int i = 0; i < 12; i++) {
            list.remove((Integer) cells[i][col]);
            list.remove((Integer) cells[row][i]);
        }
        int r2 = row / 3 * 3;
        int c2 = col / 3 * 3;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) list.remove((Integer) cells[r2 + i][c2 + j]);
        return list;
    }

    private Set<Integer> checkPossibles(int row, int col) {
        Set<Integer> set = new HashSet<>(ONE2NINE);
        for (int i = 0; i < 12; i++) {
            set.remove(cells[i][col]);
            set.remove(cells[row][i]);
        }
        int r2 = row / 3 * 3;
        int c2 = col / 3 * 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                set.remove(cells[r2 + i][c2 + j]);
            }
        }
        /*if (set.isEmpty()) {
            System.out.println("Row:" + row + ",Col:" + col + " possibles is empty!");
        }*/
        return set;
    }

    private Set<Integer> checkLastPossibles(int row, int col) {
        Set<Integer> set = new HashSet<>(ONE2NINE);
        int max_row;
        if (row >= 9) {
            max_row = 12;
        } else if (layout[row / 3 + 1] == col / 3) {
            max_row = (row / 3 + 3) * 3;
        } else {
            max_row = (row / 3 + 2) * 3;
        }

        for (int i = 0; i < 12; i++) {
            if (i < max_row) set.remove(cells[i][col]);
            set.remove(cells[row][i]);
        }
        int r2 = row / 3 * 3;
        int c2 = col / 3 * 3;
        for (int i = 0; i < 3; i++) {
            if (r2 + i < max_row) {
                for (int j = 0; j < 3; j++) {
                    set.remove(cells[r2 + i][c2 + j]);
                }
            }
        }
        return set;
    }

    public void initNewGame() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                cells[i][j] = 0;
                final int row = i, col = j;
                Platform.runLater(() -> boxes[row][col].setText(""));
            }
        }
        //stacks.clear();
        generateRandLayout();
        Platform.runLater(this::updateLayoutUI);
        //genRandQueue();
        //mask.setFill(Color.TRANSPARENT);
        //mask.setLayoutX(0);
        //mask.setLayoutY(0);
    }

    private void generateSoduku(int level) {
        initNewGame();

        level = level < 17 ? 17 : level > 108 ? 108 : level;

        while (stacks.size() < 108) {
            if (!pause.get()) goNextStep();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO 前进步用蓝色表示，后退步用橙红色表示
    private void forward(int row, int col, int num, int next) {
        cells[row][col] = num;
        Step step = new Step(row, col, num, next < 108 ? checkPossibles(rowQueue[next], colQueue[next]) : new HashSet<>());
        stacks.push(step);
        System.out.println(next - 1);
        //System.out.println("-->:" + (next - 1) + "," + step + "," + step.possibles);
        Platform.runLater(() -> {
            forwardStepUI(step);
            updateNextStepUI(rowQueue[next], colQueue[next]);
        });
    }

    private void updateNextStepUI(int row, int col) {
        nextMask.setFill(Color.rgb(255, 200, 0));
        nextMask.setLayoutX(layoutX + cellWidth * col);
        nextMask.setLayoutY(layoutY + cellWidth * row);
    }

    private void backward(int row, int col) {
        Step fail = stacks.pop();
        cells[fail.row][fail.col] = 0;
        stacks.peek().possibles.remove(fail.num);
        while (!interfere(fail.row, fail.col, row, col)) {
            fail = stacks.pop();
            cells[fail.row][fail.col] = 0;
            stacks.peek().possibles.remove(fail.num);
        }
        System.out.println(stacks.size() - 1);
        //System.out.println("<--:" + (stacks.size() - 1) + stacks.peek() + "," + stacks.peek().possibles);
        //Platform.runLater(() -> backwardStepUI(fail));
    }

    public static boolean interfere(int row1, int col1, int row2, int col2) {
        if (row1 == row2 || col1 == col2) return true;
        int r2 = row2 / 3 * 3;
        int c2 = col2 / 3 * 3;
        return row1 >= r2 && row1 - r2 < 3 && col1 >= c2 && col1 - c2 < 3;
    }

    public boolean canPlace(int row, int col, int num) {
        int r1 = row / 3, c1 = col / 3;
        // TODO KEEP if (layout[r1] == c1) return num == 0;
        for (int i = 0; i < 12; i++) {
            if (i != row && cells[i][col] == num) return false;
            if (i != col && cells[row][i] == num) return false;
        }
        for (int i = 0; i < 3; i++) {
            int r2 = r1 * 3 + i;
            for (int j = 0; j < 3; j++) {
                int c2 = c1 * 3 + j;
                if (r2 == row && c2 == col) continue;
                if (cells[r2][c2] == num) return false;
            }
        }
        return true;
    }

    public void generateRandLayout() {
        List<Integer> list = getRandQueue(0, 3);
        layout[0] = list.get(0);
        layout[1] = list.get(1);
        layout[2] = list.get(2);
        layout[3] = list.get(3);
    }

    public boolean checkSoduku() {
        for (int i = 0; i < 12; i++) {
            if (!checkRow(i)) return false;
            if (!checkColumn(i)) return false;
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (!checkSquare(3 * i, 3 * j)) return false;
            }
        }
        return true;
    }

    public boolean checkColumn(int column) {
        short word = 0;
        for (int i = 0; i < 12; i++) word |= 1 << cells[i][column];
        return word == 1023;
    }

    public boolean checkRow(int row) {
        short word = 0;
        for (int i = 0; i < 12; i++) word |= 1 << cells[row][i];
        return word == 1023;
    }

    public boolean checkSquare(int row, int column) {
        short word = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                word |= 1 << cells[row + i][column + j];
            }
        }
        return word == 1022 || word == 1;
    }

    public void pauseGame() {
        pause.set(!pause.get());
        pause_button.setText(pause.get() ? "运行" : "暂停");
    }

    private void genRandQueue() {
        List<Integer> list = getRandQueue(0, 143);
        int index = 0;
        for (int num : list) {
            if (layout[num / 36] != num % 12 / 3) {
                rowQueue[index] = num / 12;
                colQueue[index] = num % 12;
                index++;
            }
        }
    }

    public void genFirstFourMatrix() {
        initNewGame();
        line = 0;
        //generateRandLayout();
        //updateLayoutUI();
        for (int row = 0; row < 4; row++) {
            int col = layout[row] + 1;
            if (col >= 4) col = col - 4;
            List<Integer> list = genRandMatrix();
            for (int i = 0; i < 3; i++) {
                int r1 = row * 3 + i;
                for (int j = 0; j < 3; j++) {
                    int c1 = col * 3 + j;
                    cells[r1][c1] = list.get(3 * i + j);
                    Platform.runLater(() -> boxes[r1][c1].setText(String.valueOf(cells[r1][c1])));
                }
            }
        }
    }

    public void exportToFile() {
        File file = new File("map.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            for (int i = 0; i < 12; i++) {
                for (int j = 0; j < 12; j++) {
                    writer.write(Integer.toString(cells[i][j]));
                    writer.write(' ');
                }
                writer.write('\n');
            }
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void reGenNextFour() {
        for (int row = 0; row < 4; row++) {
            int col1 = layout[row] + 2;
            if (col1 >= 4) col1 = col1 - 4;
            int col2 = layout[row] + 3;
            if (col2 >= 4) col2 = col2 - 4;
            for (int i = 0; i < 3; i++) {
                int r1 = row * 3 + i;
                for (int j = 0; j < 3; j++) {
                    int c1 = col1 * 3 + j;
                    int c2 = col2 * 3 + j;
                    cells[r1][c1] = 0;
                    cells[r1][c2] = 0;
                    Platform.runLater(() -> {
                        boxes[r1][c1].setText("");
                        boxes[r1][c2].setText("");
                    });
                }
            }
        }
        genNextFourMatrix();
    }

    public void genAllFour() {
        initNewGame();
        new Thread(() -> {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    if (layout[row] != col) {
                        int code = genTheFour(row, col);
                        System.out.println("row:" + row + ",col:" + col + ",code:" + code);
                        if (code == -1) {
                            col -= 2;
                            if (layout[row] == col + 1) {
                                col -= 1;
                            }
                            if (col + 1 < 0) {
                                col = col + 4;
                                if (layout[row] == col + 1) {
                                    col -= 1;
                                }
                                row = row - 1;
                            }
                            continue;
                        } else if (code == 0) {
                            col -= 1;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void genAllSolutions() {
        getAllSolutions(0, 0);
    }

    public void generateMatrix() {
        new Thread(() -> {
            initNewGame();
            checkSetGrid(0, 143);
        }).start();
    }

    private boolean checkSetGrid(int index, final int last) {
        int row = index / 12, col = index % 12;
        if (layout[row / 3] == col / 3) return index == last || checkSetGrid(index + 1, last);
        ArrayList<Integer> possibles = checkRandPossibles(row, col);
        for (int num : possibles) {
            cells[row][col] = num;
            Platform.runLater(() -> boxes[row][col].setText(String.valueOf(num)));
            if (index == last) return true;
            if (checkSetGrid(index + 1, last)) return true;
            cells[row][col] = 0;
            Platform.runLater(() -> boxes[row][col].setText(""));
        }
        return false;
    }

    private void geneFirstGrid(int row, int col) {
        List<Integer> queue = getRandQueue(1, 9);
        for (int i = 0; i < 3; i++) {
            int r1 = row * 3 + i;
            for (int j = 0; j < 3; j++) {
                int c1 = col * 3 + j;
                cells[r1][c1] = queue.get(3 * i + j);
                Platform.runLater(() -> {
                    if (cells[r1][c1] != 0) boxes[r1][c1].setText(String.valueOf(cells[r1][c1]));
                });
            }
        }
    }

    private ArrayList<ArrayList<Integer>> getAllSolutions(int row, int col) {
        int[] solution = new int[9];
        ArrayList<ArrayList<Integer>> solutions = new ArrayList<>();
        doSth(row, col, 0, solution, solutions);
        return solutions;
    }

    private void doSth(int row, int col, int i, int[] solution, ArrayList<ArrayList<Integer>> solutions) {
        int r1 = row * 3 + i / 3;
        int c1 = col * 3 + i % 3;
        Set<Integer> possibles = checkPossibles(r1, c1);
        for (int num : possibles) {
            cells[r1][c1] = num;
            solution[i] = num;
            if (i < 8) doSth(row, col, i + 1, solution, solutions);
            else {
                ArrayList<Integer> list = new ArrayList<>();
                for (int n : solution) list.add(n);
                solutions.add(list);
            }
            cells[r1][c1] = 0;
            solution[i] = 0;
        }
    }

    public void genNextFourMatrix() {
        for (int row = 0; row < 4; row++) {
            int col = layout[row] + 2;
            if (col >= 4) col = col - 4;
            while (genTheFour(row, col) != 1) ;
        }
    }

    private int line = 0;

    public void calculateLastFour() {
        if (line > 3) return;
        /*for (int row = 0; row < 4; row++) {
        }*/
        int row = line;
        int col = layout[line++] + 3;
        if (col >= 4) col = col - 4;
        for (int i = 0; i < 3; i++) {
            int r1 = row * 3 + i;
            int c1 = col * 3;
            Set<Integer> ps1 = checkLastPossibles(r1, c1);
            if (ps1.size() == 1) {
                int num = ps1.iterator().next();
                cells[r1][c1] = num;
                ps1.remove(num);
            }
            Set<Integer> ps2 = checkLastPossibles(r1, c1 + 1);
            if (ps2.size() == 1) {
                int num = ps2.iterator().next();
                cells[r1][c1 + 1] = num;
                ps1.remove(num);
                ps2.remove(num);
            }
            Set<Integer> ps3 = checkLastPossibles(r1, c1 + 2);
            if (ps3.size() == 1) {
                int num = ps3.iterator().next();
                cells[r1][c1 + 2] = num;
                ps1.remove(num);
                ps2.remove(num);
                ps3.remove(num);
            }


            if (ps1.size() >= 1) {
                Set<Integer> ps = checkPossibles(r1, c1);
                int num = ps.size() >= 1 ? ps.iterator().next() : ps1.iterator().next();
                cells[r1][c1] = num;
                ps1.remove(num);
                ps2.remove(num);
                ps3.remove(num);
            }
            if (ps2.size() >= 1) {
                Set<Integer> ps = checkPossibles(r1, c1 + 1);
                int num = ps.size() >= 1 ? ps.iterator().next() : ps2.iterator().next();
                cells[r1][c1 + 1] = num;
                ps1.remove(num);
                ps2.remove(num);
                ps3.remove(num);
            }
            if (ps3.size() >= 1) {
                Set<Integer> ps = checkPossibles(r1, c1 + 2);
                int num = ps.size() >= 1 ? ps.iterator().next() : ps3.iterator().next();
                cells[r1][c1 + 2] = num;
                ps1.remove(num);
                ps2.remove(num);
                ps3.remove(num);
            }

//            if (ps1.size() == 1) cells[r1][c1] = ps1.iterator().next();
//            if (ps2.size() == 1) cells[r1][c1 + 1] = ps2.iterator().next();
//            if (ps3.size() == 1) cells[r1][c1 + 2] = ps3.iterator().next();
//
//            if (cells[r1][c1] == 0 && ps1.size() > 0) cells[r1][c1] = ps1.iterator().next();
//
//            if (cells[r1][c1 + 1] == 0 && ps1.size() > 0) cells[r1][c1 + 1] = ps1.iterator().next();
//            if (cells[r1][c1 + 2] == 0 && ps1.size() > 0) cells[r1][c1 + 2] = ps1.iterator().next();

            Platform.runLater(() -> {
                if (cells[r1][c1] != 0) boxes[r1][c1].setText(String.valueOf(cells[r1][c1]));
                if (cells[r1][c1 + 1] != 0) boxes[r1][c1 + 1].setText(String.valueOf(cells[r1][c1 + 1]));
                if (cells[r1][c1 + 2] != 0) boxes[r1][c1 + 2].setText(String.valueOf(cells[r1][c1 + 2]));
            });
        }
    }

    public Set<Integer> getRowPossibles(int row) {
        Set<Integer> possibles = new HashSet<>(ONE2NINE);
        for (int i = 0; i < 12; i++) {
            possibles.remove(cells[row][i]);
        }
        return possibles;
    }

    private void cleanTheFour(int row, int col) {
        if (row < 0 || col < 0 || row > 3 || col > 3) {
            System.out.println("Invalid Axis:row:" + row + ",col:" + col);
            return;
        }
        for (int i = 0; i < 3; i++) {
            int r1 = row * 3 + i;
            for (int j = 0; j < 3; j++) {
                int c1 = col * 3 + j;
                cells[r1][c1] = 0;
                Platform.runLater(() -> boxes[r1][c1].setText(""));
            }
        }
    }

    private int genTheFour(int row, int col) {
        cleanTheFour(row, col);
        for (int i = 0; i < 3; i++) {
            int r1 = row * 3 + i;
            for (int j = 0; j < 3; j++) {
                int c1 = col * 3 + j;
                if (checkPossibles(r1, c1).isEmpty()) return -1;
            }
        }

        for (int i = 0; i < 3; i++) {
            int r1 = row * 3 + i;
            COLUMN:
            for (int j = 0; j < 3; j++) {
                int c1 = col * 3 + j;
                List<Integer> queue = getRandQueue(1, 9);
                for (int num : queue) {
                    if (canPlace(r1, c1, num)) {
                        cells[r1][c1] = num;
                        Platform.runLater(() -> boxes[r1][c1].setText(String.valueOf(cells[r1][c1])));
                        continue COLUMN;
                    }
                }
                //Platform.runLater(() -> boxes[r1][c1].setText("N"));
                //System.out.println("row:" + r1 + ",col:" + c1 + " NOT Possible !");
                return 0;
            }
        }
        return 1;
    }

    public void reGenLastFour() {
        for (int row = 0; row < 4; row++) {
            int col = layout[row] + 3;
            if (col >= 4) col = col - 4;
            for (int i = 0; i < 3; i++) {
                int r1 = row * 3 + i;
                for (int j = 0; j < 3; j++) {
                    int c1 = col * 3 + j;
                    cells[r1][c1] = 0;
                    Platform.runLater(() -> boxes[r1][c1].setText(""));
                }
            }
        }
        genLastFourMatrix();
    }

    public void genLastFourMatrix() {
        for (int row = 0; row < 4; row++) {
            int col = layout[row] + 3;
            if (col >= 4) col = col - 4;
            for (int i = 0; i < 3; i++) {
                int r1 = row * 3 + i;
                COLUMN:
                for (int j = 0; j < 3; j++) {
                    int c1 = col * 3 + j;
                    List<Integer> queue = getRandQueue(1, 9);
                    for (int num : queue) {
                        if (canPlace(r1, c1, num)) {
                            cells[r1][c1] = num;
                            Platform.runLater(() -> boxes[r1][c1].setText(String.valueOf(cells[r1][c1])));
                            continue COLUMN;
                        }
                    }
                    Platform.runLater(() -> boxes[r1][c1].setText("N"));
                    System.out.println("row:" + r1 + ",col:" + c1 + " NOT Possible !");
                    return;
                }
            }
        }
    }

    private ArrayList<Integer> genRandMatrix() {
        Random random = new Random();
        ArrayList<Integer> list = new ArrayList<>();
        while (list.size() < 9) {
            int rand = random.nextInt(9) + 1;
            if (!list.contains(rand)) list.add(rand);
        }
        return list;
    }

    private static ArrayList<Integer> getRandQueue(int min, int max) {
        ArrayList<Integer> array = new ArrayList<>();
        ArrayList<Integer> list = new ArrayList<>();
        Random random = new Random(System.nanoTime());
        for (int i = min; i <= max; i++) list.add(i);
        int length = list.size();
        while (length > 0) {
            int index = random.nextInt(length);
            array.add(list.remove(index));
            length--;
        }
        return array;
    }
}
