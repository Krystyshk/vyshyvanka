import java.awt.Color;
import java.util.Random;

public class PatternGenerator {
    private static final Color RED = new Color(172, 58, 45);
    private static final Color DARK_RED = new Color(120, 0, 25);
    private static final Color BLACK = new Color(25, 25, 25);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLUE = new Color(31, 73, 81);
    private static final Color GREEN = new Color(62, 128, 95);
    private static final Color GOLD = new Color(225, 168, 65);

    public static void generateTraditionalOrnament(EmbroideryModel model) {
        model.clear();

        int rows = model.getRows();
        int cols = model.getCols();

        int centerRow = rows / 2;
        int centerCol = cols / 2;

        drawDiamond(model, centerRow, centerCol, 15, RED);
        drawDiamond(model, centerRow, centerCol, 11, BLUE);
        drawDiamond(model, centerRow, centerCol, 7, GOLD);
        drawDiamond(model, centerRow, centerCol, 3, DARK_RED);

        drawCross(model, centerRow, centerCol, 18, BLACK);
        drawCross(model, centerRow, centerCol, 10, RED);

        drawSmallStars(model, centerRow, centerCol);
        drawBorder(model);
    }

    public static void generateBirdOrnament(EmbroideryModel model) {
        model.clear();

        int rows = model.getRows();
        int cols = model.getCols();

        drawBorder(model);

        int centerRow = rows / 2;
        int centerCol = cols / 2;

        drawDiamond(model, centerRow, centerCol, 12, RED);
        drawDiamond(model, centerRow, centerCol, 8, BLUE);
        drawDiamond(model, centerRow, centerCol, 4, GOLD);

        drawBird(model, centerRow - 8, centerCol - 30, false);
        drawBird(model, centerRow - 8, centerCol + 30, true);

        drawBird(model, centerRow + 14, centerCol - 30, false);
        drawBird(model, centerRow + 14, centerCol + 30, true);
    }

    private static void drawBird(EmbroideryModel model, int row, int col, boolean mirror) {
        int direction = mirror ? -1 : 1;

        put(model, row, col, BLUE);
        put(model, row, col + direction, BLUE);
        put(model, row, col + 2 * direction, BLUE);

        put(model, row - 1, col + 2 * direction, BLUE);
        put(model, row - 2, col + 3 * direction, BLUE);
        put(model, row - 3, col + 4 * direction, RED);

        put(model, row + 1, col, RED);
        put(model, row + 1, col + direction, RED);
        put(model, row + 2, col + direction, GOLD);

        put(model, row - 1, col - direction, BLACK);
        put(model, row - 2, col - 2 * direction, BLACK);

        put(model, row + 2, col - direction, BLACK);
        put(model, row + 3, col - direction, BLACK);
    }

    private static void put(EmbroideryModel model, int row, int col, Color color) {
        model.setCell(row, col, color);
    }

    private static void drawDiamond(EmbroideryModel model, int centerRow, int centerCol, int radius, Color color) {
        for (int row = -radius; row <= radius; row++) {
            for (int col = -radius; col <= radius; col++) {
                if (Math.abs(row) + Math.abs(col) == radius) {
                    model.setCell(centerRow + row, centerCol + col, color);
                }
            }
        }
    }

    private static void drawCross(EmbroideryModel model, int centerRow, int centerCol, int size, Color color) {
        for (int i = -size; i <= size; i++) {
            model.setCell(centerRow, centerCol + i, color);
            model.setCell(centerRow + i, centerCol, color);

            if (i % 2 == 0) {
                model.setCell(centerRow + i, centerCol + i, color);
                model.setCell(centerRow + i, centerCol - i, color);
            }
        }
    }

    private static void drawSmallStars(EmbroideryModel model, int centerRow, int centerCol) {
        int offsetRow = 18;
        int offsetCol = 30;

        drawStar(model, centerRow - offsetRow, centerCol - offsetCol, RED);
        drawStar(model, centerRow - offsetRow, centerCol + offsetCol, RED);
        drawStar(model, centerRow + offsetRow, centerCol - offsetCol, RED);
        drawStar(model, centerRow + offsetRow, centerCol + offsetCol, RED);
    }

    private static void drawStar(EmbroideryModel model, int row, int col, Color color) {
        model.setCell(row, col, color);
        model.setCell(row - 1, col, color);
        model.setCell(row + 1, col, color);
        model.setCell(row, col - 1, color);
        model.setCell(row, col + 1, color);

        model.setCell(row - 2, col, BLACK);
        model.setCell(row + 2, col, BLACK);
        model.setCell(row, col - 2, BLACK);
        model.setCell(row, col + 2, BLACK);
    }

    private static void drawBorder(EmbroideryModel model) {
        int rows = model.getRows();
        int cols = model.getCols();

        for (int col = 0; col < cols; col++) {
            if (col % 4 == 0) {
                model.setCell(0, col, RED);
                model.setCell(rows - 1, col, RED);
            }

            if (col % 4 == 1) {
                model.setCell(1, col, BLUE);
                model.setCell(rows - 2, col, BLUE);
            }

            if (col % 4 == 2) {
                model.setCell(2, col, GOLD);
                model.setCell(rows - 3, col, GOLD);
            }
        }

        for (int row = 0; row < rows; row++) {
            if (row % 4 == 0) {
                model.setCell(row, 0, BLACK);
                model.setCell(row, cols - 1, BLACK);
            }

            if (row % 4 == 1) {
                model.setCell(row, 1, RED);
                model.setCell(row, cols - 2, RED);
            }
        }
    }

    public static void generateDuplicatedFragment(
            EmbroideryModel model,
            int fragmentRows,
            int fragmentCols,
            SymmetryMode mode
    ) {
        model.clear();

        Random random = new Random();

        Color[] colors = {
                RED,
                BLACK,
                BLUE,
                GREEN,
                GOLD,
                DARK_RED
        };

        Color[][] fragment = new Color[fragmentRows][fragmentCols];

        for (int row = 0; row < fragmentRows; row++) {
            for (int col = 0; col < fragmentCols; col++) {
                boolean shouldPaint = random.nextInt(100) < 38;

                if (shouldPaint) {
                    if (row == col || row + col == fragmentCols - 1) {
                        fragment[row][col] = BLACK;
                    } else if (row == fragmentRows / 2 || col == fragmentCols / 2) {
                        fragment[row][col] = RED;
                    } else {
                        fragment[row][col] = colors[random.nextInt(colors.length)];
                    }
                } else {
                    fragment[row][col] = WHITE;
                }
            }
        }

        applySymmetryToFragment(fragment, mode);

        for (int row = 0; row < model.getRows(); row += fragmentRows) {
            for (int col = 0; col < model.getCols(); col += fragmentCols) {
                copyFragment(model, fragment, row, col);
            }
        }
    }

    private static void applySymmetryToFragment(Color[][] fragment, SymmetryMode mode) {
        int rows = fragment.length;
        int cols = fragment[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color color = fragment[row][col];

                if (mode == SymmetryMode.HORIZONTAL || mode == SymmetryMode.BOTH) {
                    fragment[rows - 1 - row][col] = color;
                }

                if (mode == SymmetryMode.VERTICAL || mode == SymmetryMode.BOTH) {
                    fragment[row][cols - 1 - col] = color;
                }

                if (mode == SymmetryMode.BOTH) {
                    fragment[rows - 1 - row][cols - 1 - col] = color;
                }
            }
        }
    }

    private static void copyFragment(EmbroideryModel model, Color[][] fragment, int startRow, int startCol) {
        for (int row = 0; row < fragment.length; row++) {
            for (int col = 0; col < fragment[row].length; col++) {
                model.setCell(startRow + row, startCol + col, fragment[row][col]);
            }
        }
    }
}