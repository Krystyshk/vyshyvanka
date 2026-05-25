import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HomeFrame frame = new HomeFrame();
            frame.setVisible(true);
        });
    }
}