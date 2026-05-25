import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;

public class PalettePanel extends JPanel {
    private final Consumer<Color> onColorSelected;

    public PalettePanel(Consumer<Color> onColorSelected) {
        this.onColorSelected = onColorSelected;

        setLayout(new GridLayout(0, 2, 8, 8));

        add(new JLabel("Палітра:"));

        addColorButton("Червоний", new Color(190, 30, 45));
        addColorButton("Чорний", Color.BLACK);
        addColorButton("Білий", Color.WHITE);
        addColorButton("Сірий", new Color(120, 120, 120));
        addColorButton("Бордо", new Color(120, 0, 25));
        addColorButton("Синій", new Color(35, 70, 150));
        addColorButton("Зелений", new Color(20, 120, 70));
        addColorButton("Жовтий", new Color(235, 190, 50));
        addColorButton("Бежевий", new Color(226, 209, 176));

        JButton customColor = new JButton("Свій колір");
        customColor.addActionListener(e -> {
            Color color = JColorChooser.showDialog(
                    this,
                    "Оберіть колір",
                    new Color(190, 30, 45)
            );

            if (color != null) {
                onColorSelected.accept(color);
            }
        });

        add(customColor);
    }

    private void addColorButton(String name, Color color) {
        JButton button = new JButton(name);
        button.setBackground(color);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(110, 35));

        if (isDark(color)) {
            button.setForeground(Color.WHITE);
        } else {
            button.setForeground(Color.BLACK);
        }

        button.addActionListener(e -> onColorSelected.accept(color));
        add(button);
    }

    private boolean isDark(Color color) {
        int brightness = color.getRed() + color.getGreen() + color.getBlue();
        return brightness < 380;
    }
}