import java.awt.Color;

public class EmbroideryModel {
    private int rows;
    private int cols;
    private Color[][] cells;

    public EmbroideryModel(int rows, int cols) {
        resize(rows, cols);
    }

    public void resize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        cells = new Color[rows][cols];
        clear();
    }

    public void clear() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells[row][col] = Color.WHITE;
            }
        }
    }

    public void setCell(int row, int col, Color color) {
        if (isInside(row, col)) {
            cells[row][col] = color;
        }
    }

    public Color getCell(int row, int col) {
        if (isInside(row, col)) {
            return cells[row][col];
        }
        return Color.WHITE;
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}