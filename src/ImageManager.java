import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageManager {
    public static void saveAsPng(
            EmbroideryModel model,
            File file,
            int cellSize
    ) throws IOException {
        int width = model.getCols() * cellSize;
        int height = model.getRows() * cellSize;

        BufferedImage image = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2 = image.createGraphics();

        for (int row = 0; row < model.getRows(); row++) {
            for (int col = 0; col < model.getCols(); col++) {
                g2.setColor(model.getCell(row, col));
                g2.fillRect(
                        col * cellSize,
                        row * cellSize,
                        cellSize,
                        cellSize
                );

                g2.setColor(new Color(220, 220, 220));
                g2.drawRect(
                        col * cellSize,
                        row * cellSize,
                        cellSize,
                        cellSize
                );
            }
        }

        g2.dispose();

        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getAbsolutePath() + ".png");
        }

        ImageIO.write(image, "png", file);
    }

    public static void openFromPng(
            EmbroideryModel model,
            File file
    ) throws IOException {
        BufferedImage image = ImageIO.read(file);

        if (image == null) {
            throw new IOException("Файл не є зображенням PNG.");
        }

        int cellWidth = Math.max(1, image.getWidth() / model.getCols());
        int cellHeight = Math.max(1, image.getHeight() / model.getRows());

        for (int row = 0; row < model.getRows(); row++) {
            for (int col = 0; col < model.getCols(); col++) {
                int x = Math.min(
                        col * cellWidth + cellWidth / 2,
                        image.getWidth() - 1
                );

                int y = Math.min(
                        row * cellHeight + cellHeight / 2,
                        image.getHeight() - 1
                );

                Color color = new Color(image.getRGB(x, y));
                model.setCell(row, col, color);
            }
        }
    }
}