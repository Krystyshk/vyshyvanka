import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GridPanel extends JPanel {
    private final EmbroideryModel model;

    private Color selectedColor = new Color(190, 30, 45);
    private SymmetryMode symmetryMode = SymmetryMode.NONE;

    private boolean eraserMode = false;
    private boolean showGrid = true;

    private int cellSize = 10;

    public GridPanel(EmbroideryModel model) {
        this.model = model;
        setBackground(Color.WHITE);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                paintByMouse(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                paintByMouse(e);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private void paintByMouse(MouseEvent e) {
        int col = e.getX() / cellSize;
        int row = e.getY() / cellSize;

        if (row < 0 || row >= model.getRows() || col < 0 || col >= model.getCols()) {
            return;
        }

        Color colorToPaint;

        if (eraserMode) {
            colorToPaint = Color.WHITE;
        } else {
            colorToPaint = selectedColor;
        }

        paintWithSymmetry(row, col, colorToPaint);
        repaint();
    }

    private void paintWithSymmetry(int row, int col, Color color) {
        int maxRow = model.getRows() - 1;
        int maxCol = model.getCols() - 1;

        model.setCell(row, col, color);

        if (symmetryMode == SymmetryMode.HORIZONTAL || symmetryMode == SymmetryMode.BOTH) {
            model.setCell(maxRow - row, col, color);
        }

        if (symmetryMode == SymmetryMode.VERTICAL || symmetryMode == SymmetryMode.BOTH) {
            model.setCell(row, maxCol - col, color);
        }

        if (symmetryMode == SymmetryMode.BOTH) {
            model.setCell(maxRow - row, maxCol - col, color);
        }
    }

    public void setZoomCellSize(int newSize) {
        cellSize = newSize;
        revalidate();
        repaint();
    }

    public int getZoomCellSize() {
        return cellSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );

        for (int row = 0; row < model.getRows(); row++) {
            for (int col = 0; col < model.getCols(); col++) {
                int x = col * cellSize;
                int y = row * cellSize;

                g2.setColor(model.getCell(row, col));
                g2.fillRect(x, y, cellSize, cellSize);

                if (showGrid) {
                    g2.setColor(new Color(220, 220, 220));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRect(x, y, cellSize, cellSize);
                }
            }
        }

        g2.setColor(new Color(90, 90, 90));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(
                0,
                0,
                model.getCols() * cellSize,
                model.getRows() * cellSize
        );
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                model.getCols() * cellSize,
                model.getRows() * cellSize
        );
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
        this.eraserMode = false;
    }

    public void setSymmetryMode(SymmetryMode symmetryMode) {
        this.symmetryMode = symmetryMode;
    }

    public SymmetryMode getSymmetryMode() {
        return symmetryMode;
    }

    public void setEraserMode(boolean eraserMode) {
        this.eraserMode = eraserMode;
    }

    public void toggleGrid() {
        showGrid = !showGrid;
        repaint();
    }
}