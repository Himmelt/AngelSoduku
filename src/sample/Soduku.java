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
    private static final List<Integer> ZERO2NINE = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

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
            backward();
        }
    }

    private Set<Integer> checkPossibles(int row, int col) {
        Set<Integer> set = new HashSet<>(ZERO2NINE);
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

    public void initNewGame() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                cells[i][j] = 0;
                final int row = i, col = j;
                Platform.runLater(() -> boxes[row][col].setText(""));
            }
        }
        stacks.clear();
        generateRandLayout();
        Platform.runLater(this::updateLayoutUI);
        genRandQueue();
        mask.setFill(Color.TRANSPARENT);
        mask.setLayoutX(0);
        mask.setLayoutY(0);
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
        Platform.runLater(() -> {
            forwardStepUI(step);
            //updateNextStepUI(rowQueue[next], colQueue[next]);
        });
    }

    private void updateNextStepUI(int row, int col) {
        nextMask.setFill(Color.rgb(255, 200, 0));
        nextMask.setLayoutX(layoutX + cellWidth * col);
        nextMask.setLayoutY(layoutY + cellWidth * row);
    }

    private void backward() {
        Step fail = stacks.pop();
        cells[fail.row][fail.col] = 0;
        stacks.peek().possibles.remove(fail.num);
        Platform.runLater(() -> backwardStepUI(fail));
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
        Random random = new Random();
        List<Integer> list = new ArrayList<>();
        while (list.size() < 4) {
            int num = random.nextInt(4);
            if (!list.contains(num)) list.add(num);
        }
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

    private static List<Integer> getRandQueue(int min, int max) {
        List<Integer> array = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
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
