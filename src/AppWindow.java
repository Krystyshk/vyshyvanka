import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class AppWindow {
    public static final int WINDOW_W = 1250;
    public static final int WINDOW_H = 807;

    public static void setup(JFrame frame) {
        frame.setSize(WINDOW_W, WINDOW_H);
        frame.setResizable(false);
        center(frame);
    }

    public static void open(JFrame currentFrame, JFrame nextFrame) {
        setup(nextFrame);
        nextFrame.setVisible(true);
        currentFrame.dispose();
    }

    private static void center(JFrame frame) {
        Rectangle screen = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();

        int x = screen.x + (screen.width - WINDOW_W) / 2;
        int y = screen.y + (screen.height - WINDOW_H) / 2;

        if (x < screen.x) {
            x = screen.x;
        }

        if (y < screen.y) {
            y = screen.y;
        }

        frame.setLocation(x, y);
    }
}
