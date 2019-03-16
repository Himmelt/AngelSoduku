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

public class SodukuController implements Initializable {

    private static int[] layout = new int[4];
    private static int[][] cells = new int[12][12];
    private static List<Integer>[][] possibles = new List[12][12];
    private static Rectangle[][] masks = new Rectangle[4][4];
    private static TextField[][] boxes = new TextField[12][12];
    private static final double layoutX = 300;
    private static final double layoutY = 80;
    private static final double cellWidth = 40;
    private static final List<Integer> ZERO2NINE = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final AtomicBoolean pause = new AtomicBoolean(true);
    private static SodukuController INSTANCE;
    private static final Stack<Step> stacks = new Stack<>();

    public AnchorPane root;
    public TextField hard;
    public Button pause_button;
    public Label step_label;

    private static int current = 0;

    static {
        layout[0] = 0;
        layout[1] = 1;
        layout[2] = 2;
        layout[3] = 3;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                cells[i][j] = 0;
                possibles[i][j] = new ArrayList<>(ZERO2NINE);
            }
        }
    }

    public SodukuController() {
        INSTANCE = this;
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

    public static void updateLayoutUI() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                masks[i][j].setFill(layout[i] == j ? Color.LIGHTGRAY : Color.TRANSPARENT);
            }
        }
    }

    public static void updateStepUI(Step step) {
        if (step.num == 0) boxes[step.row][step.col].setText("");
        else boxes[step.row][step.col].setText(String.valueOf(step.num));
        INSTANCE.step_label.setText(step.toString());
    }

    public static void updateCellUI(int row, int col, int num) {
        if (num == 0) boxes[row][col].setText("");
        else boxes[row][col].setText(String.valueOf(num));
    }

    public static void updateStepLabel(Step step) {
        INSTANCE.step_label.setText(step.toString());
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
                generateRandSoduku(Integer.parseInt(text));
            } else generateRandSoduku(17);
        }).start();
        System.out.println("Running....");
    }

    public static void calculateNextStep() {

    }

    public static void generateRandSoduku(int start) {
        start = start < 17 ? 17 : start > 108 ? 108 : start;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                cells[i][j] = 0;
                possibles[i][j] = new ArrayList<>(ZERO2NINE);
                final int row = i, col = j;
                Platform.runLater(() -> updateCellUI(row, col, 0));
            }
        }
        stacks.clear();
        generateRandLayout();
        Platform.runLater(SodukuController::updateLayoutUI);
        current = 0;
        List<Integer> queue = getRandQueue();

        while (current < start) {
            if (pause.get()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            int row = queue.get(current) / 12;
            int col = queue.get(current) - 12 * row;
            if (layout[row / 3] == col / 3) {
                // 如果是空白区
                queue.remove(current);
                continue;
            }
            boolean success = false;
            for (int num : possibles[row][col]) {
                if (canPlace(row, col, num)) {
                    Step step = new Step(row, col, num);
                    forward(step);
                    Platform.runLater(() -> updateStepUI(step));
                    current++;
                    success = true;
                    break;
                }
            }
            if (!success) {
                Step step = stacks.pop();
                backward(step);
                Platform.runLater(() -> updateStepUI(step));
                // TODO 排除死局
                current--;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void forward(Step step) {
        // TODO 前进步用蓝色表示，后退步用橙红色表示
        cells[step.row][step.col] = step.num;
        stacks.push(step);
        updatePossibles(step);
    }

    public static void backward(Step step) {
        cells[step.row][step.col] = 0;
        revertPossibles(step);
    }

    public static void updatePossibles(Step step) {
        int row = step.row;
        int col = step.col;
        int num = step.num;
        int r1 = row / 3, c1 = col / 3;
        if (layout[r1] == c1) return;
        for (int i = 0; i < 12; i++) {
            /*if (i != row)*/
            possibles[i][col].remove((Integer) num);
            if (i != col) possibles[row][i].remove((Integer) num);
        }
        for (int i = 0; i < 3; i++) {
            int r2 = r1 * 3 + i;
            for (int j = 0; j < 3; j++) {
                int c2 = c1 * 3 + j;
                if (r2 == row && c2 == col) continue;
                possibles[r2][c2].remove((Integer) num);
            }
        }
    }

    public static void revertPossibles(Step step) {
        int row = step.row;
        int col = step.col;
        int num = step.num;
        int r1 = row / 3, c1 = col / 3;
        if (layout[r1] == c1) return;
        for (int i = 0; i < 12; i++) {
            if (i != row) possibles[i][col].add(num);
            if (i != col) possibles[row][i].add(num);
        }
        for (int i = 0; i < 3; i++) {
            int r2 = r1 * 3 + i;
            for (int j = 0; j < 3; j++) {
                int c2 = c1 * 3 + j;
                if (r2 == row && c2 == col) continue;
                possibles[r2][c2].add(num);
            }
        }
    }

    public static boolean canPlace(int row, int col, int num) {
        int r1 = row / 3, c1 = col / 3;
        if (layout[r1] == c1) return num == 0;
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

    public static void generateRandLayout() {
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

    public static boolean checkSoduku() {
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

    public static boolean checkColumn(int column) {
        short word = 0;
        for (int i = 0; i < 12; i++) word |= 1 << cells[i][column];
        return word == 1023;
    }

    public static boolean checkRow(int row) {
        short word = 0;
        for (int i = 0; i < 12; i++) word |= 1 << cells[row][i];
        return word == 1023;
    }

    public static boolean checkSquare(int row, int column) {
        short word = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                word |= 1 << cells[row + i][column + j];
            }
        }
        return word == 1022 || word == 1;
    }

    public static List<Integer> getRandQueue() {
        List<Integer> set = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 144; i++) list.add(i);
        while (set.size() < 144) {
            int index = random.nextInt(list.size());
            set.add(list.remove(index));
        }
        return set;
    }

    public void pauseGame() {
        pause.set(!pause.get());
        pause_button.setText(pause.get() ? "运行" : "暂停");
    }
}
