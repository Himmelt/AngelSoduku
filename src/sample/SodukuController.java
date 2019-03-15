package sample;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.*;

public class SodukuController implements Initializable {

    @FXML
    public TextField hard;

    private static int[] layout = new int[4];
    private static int[][] cells = new int[12][12];
    private static List<Integer>[][] possibles = new List[12][12];
    private static Rectangle[][] masks = new Rectangle[4][4];
    private static TextField[][] boxes = new TextField[12][12];
    private static final double layoutX = 300;
    private static final double layoutY = 80;
    private static final double cellWidth = 40;
    private static boolean initialized = false;

    public static final Stack<Step> stacks = new Stack<>();

    public AnchorPane root;
    public TextField input_row;
    public TextField input_col;
    public TextField input_num;

    static {
        layout[0] = 0;
        layout[1] = 0;
        layout[2] = 0;
        layout[3] = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                cells[i][j] = 0;
                possibles[i][j] = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
            }
        }
    }

    public static void init() {

    }

    public void showSoduku() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                if (cells[i][j] == 0) boxes[i][j].setText("");
                else boxes[i][j].setText(String.valueOf(cells[i][j]));
                if (i < 4 && j < 4) masks[i][j].setFill(layout[i] == j ? Color.LIGHTGRAY : Color.TRANSPARENT);
            }
        }
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
        if (text.matches("\\d+")) {
            generateRandSoduku(Integer.parseInt(text));
        } else generateRandSoduku(17);
        showSoduku();
    }

    public void checkCanPlace() {
        System.out.println(canPlace(Integer.parseInt(input_row.getText()), Integer.parseInt(input_col.getText()), Integer.parseInt(input_num.getText())));
    }

    public static void generateRandSoduku(int start) {
        start = start < 17 ? 17 : start > 108 ? 108 : start;
        layout[0] = 0;
        layout[1] = 0;
        layout[2] = 0;
        layout[3] = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                cells[i][j] = 0;
                possibles[i][j] = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
            }
        }
        generateRandLayout();
        int current = 0;
        List<Integer> queue = getRandQueue();

        while (current < start) {
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
                    forward(new Step(row, col, num));
                    current++;
                    success = true;
                    break;
                }
            }
            if (!success) {
                System.out.println("Row:" + row + ",Col:" + col + " No Possibles !");
                backward(stacks.pop());
                // TODO 排除死局
                current--;
            }
        }
    }

    public static void forward(Step step) {
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
            if (i != row) possibles[i][col].remove((Integer) num);
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

    public void initialize(URL location, ResourceBundle resources) {
        if (!initialized) {
            ObservableList<Node> pane = root.getChildren();
            root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
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
}
