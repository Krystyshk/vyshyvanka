import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class OrnamentsFrame extends JFrame {
    private final int WINDOW_W = 1250;
    private final int WINDOW_H = 807;

    private final Color bgColor = new Color(245, 241, 232);
    private final Color textRed = new Color(125, 55, 50);
    private final Color headerBlue = new Color(226, 241, 252);
    private final Color stripRed = new Color(148, 54, 48);
    private final Color borderBlue = new Color(175, 205, 230);
    private final Color darkBlue = new Color(45, 80, 90);

    private JLabel selectedLetterLabel;
    private LetterPreviewPanel previewPanel;
    private JLabel previewText;

    public OrnamentsFrame() {
        super("Вишиванка власноруч — Попкова Кристина. ІПЗ-1");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        AppWindow.setup(this);

        setContentPane(new OrnamentsPanel());
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon loadIconNoCrop(String path, int w, int h) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource(path));
            Image scaled = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    private JLabel imageLabel(String path, int w, int h) {
        JLabel label = new JLabel();
        ImageIcon icon = loadIconNoCrop(path, w, h);

        if (icon != null) {
            label.setIcon(icon);
        } else {
            label.setText("Вишиванка власноруч");
            label.setFont(new Font("Georgia", Font.BOLD, 22));
            label.setForeground(textRed);
        }

        return label;
    }

    private JButton navButton(String text, Runnable action) {
        JButton button = new JButton(text);

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(headerBlue);
        button.setForeground(textRed);
        button.setFont(new Font("Georgia", Font.BOLD, 16));
        button.setBorder(BorderFactory.createLineBorder(new Color(165, 155, 170), 2));

        button.addActionListener(e -> action.run());

        return button;
    }

    private class OrnamentsPanel extends JPanel {
        private final BufferedImage bg = loadImage("/images/home_bg.jpeg");

        public OrnamentsPanel() {
            setLayout(null);


            JLabel title = new JLabel("Орнаменти за буквами", SwingConstants.CENTER);
            title.setFont(new Font("Georgia", Font.BOLD, 42));
            title.setForeground(textRed);
            title.setBounds(0, 48, WINDOW_W, 70);
            add(title);

            int rightX = 990;
            int rightW = 180;

            JButton homeButton = navButton("На головну →", () -> {
                AppWindow.open(OrnamentsFrame.this, new HomeFrame());
            });
            homeButton.setBounds(rightX, 28, rightW, 32);
            add(homeButton);

            JButton constructorButton = navButton("Конструктор →", () -> {
                AppWindow.open(OrnamentsFrame.this, new EmbroideryFrame());
            });
            constructorButton.setBounds(rightX, 78, rightW, 32);
            add(constructorButton);

            JButton historyButton = navButton("Історія →", () -> {
                AppWindow.open(OrnamentsFrame.this, new HistoryFrame());
            });
            historyButton.setBounds(rightX, 128, rightW, 32);
            add(historyButton);

            JPanel infoBlock = new JPanel(null);
            infoBlock.setBackground(new Color(255, 255, 255, 220));
            infoBlock.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
            infoBlock.setBounds(70, 205, 720, 125);
            add(infoBlock);

            JLabel infoTitle = new JLabel("Український алфавіт у вишивці");
            infoTitle.setFont(new Font("Georgia", Font.BOLD, 28));
            infoTitle.setForeground(textRed);
            infoTitle.setBounds(20, 12, 650, 35);
            infoBlock.add(infoTitle);

            JLabel infoText = new JLabel(
                    "<html>Натисни на літеру, щоб побачити її піксельний орнамент. " +
                            "Обрана літера показується так само, як у конструкторі вишивки.</html>"
            );
            infoText.setFont(new Font("Arial", Font.PLAIN, 18));
            infoText.setForeground(Color.BLACK);
            infoText.setBounds(20, 50, 670, 65);
            infoBlock.add(infoText);

            JLabel alphabetTitle = new JLabel("Оберіть літеру");
            alphabetTitle.setFont(new Font("Georgia", Font.BOLD, 32));
            alphabetTitle.setForeground(textRed);
            alphabetTitle.setBounds(70, 380, 400, 45);
            add(alphabetTitle);

            JPanel alphabetPanel = new JPanel(new GridLayout(5, 7, 12, 12));
            alphabetPanel.setOpaque(false);
            alphabetPanel.setBounds(70, 435, 650, 255);

            String letters = "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ";

            for (int i = 0; i < letters.length(); i++) {
                char letter = letters.charAt(i);

                JButton button = new JButton(String.valueOf(letter));
                button.setFont(new Font("Georgia", Font.BOLD, 24));
                button.setForeground(darkBlue);
                button.setBackground(new Color(255, 253, 248));
                button.setFocusPainted(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                button.setBorder(BorderFactory.createLineBorder(borderBlue, 2));

                button.addActionListener(e -> showLetterPattern(button.getText()));

                alphabetPanel.add(button);
            }

            add(alphabetPanel);

            createPreviewBlock();

            showLetterPattern("А");
        }

        private void createPreviewBlock() {
            JPanel previewBlock = new JPanel(null);
            previewBlock.setBackground(new Color(255, 255, 255, 220));
            previewBlock.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
            previewBlock.setBounds(835, 315, 330, 365);
            add(previewBlock);

            JLabel title = new JLabel("Орнамент літери", SwingConstants.CENTER);
            title.setFont(new Font("Georgia", Font.BOLD, 26));
            title.setForeground(textRed);
            title.setBounds(0, 18, 330, 35);
            previewBlock.add(title);

            selectedLetterLabel = new JLabel("А", SwingConstants.CENTER);
            selectedLetterLabel.setFont(new Font("Georgia", Font.BOLD, 46));
            selectedLetterLabel.setForeground(darkBlue);
            selectedLetterLabel.setBounds(0, 55, 330, 55);
            previewBlock.add(selectedLetterLabel);

            previewPanel = new LetterPreviewPanel();
            previewPanel.setBounds(50, 118, 230, 190);
            previewBlock.add(previewPanel);

            previewText = new JLabel(
                    "<html><div style='text-align:center;'>Так літера виглядає<br>у схемі вишивки.</div></html>",
                    SwingConstants.CENTER
            );
            previewText.setFont(new Font("Arial", Font.BOLD, 15));
            previewText.setForeground(Color.BLACK);
            previewText.setBounds(20, 310, 290, 45);
            previewBlock.add(previewText);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(bgColor);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            g.setColor(headerBlue);
            g.fillRect(0, 0, getWidth(), 180);

            g.setColor(new Color(180, 210, 235));
            g.drawLine(0, 180, getWidth(), 180);

            g.setColor(stripRed);
            g.fillRoundRect(70, 350, 720, 7, 7, 7);
        }
    }

    private void showLetterPattern(String letter) {
        selectedLetterLabel.setText(letter);
        previewPanel.setPattern(letter);

        previewText.setText(
                "<html><div style='text-align:center;'>Літера \"" + letter + "\" показана так,<br>як у конструкторі.</div></html>"
        );
    }

    private class LetterPreviewPanel extends JPanel {
        private EmbroideryModel letterModel;

        public LetterPreviewPanel() {
            setOpaque(false);
        }

        public void setPattern(String letter) {
            letterModel = new EmbroideryModel(25, 25);
            NamePatternGenerator.generateName(letterModel, letter);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            g2.setColor(new Color(255, 253, 248));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(borderBlue);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

            if (letterModel == null) {
                g2.dispose();
                return;
            }

            int rows = letterModel.getRows();
            int cols = letterModel.getCols();

            int cell = Math.min((getWidth() - 30) / cols, (getHeight() - 30) / rows);

            int totalW = cols * cell;
            int totalH = rows * cell;

            int startX = (getWidth() - totalW) / 2;
            int startY = (getHeight() - totalH) / 2;

            g2.setColor(new Color(215, 215, 215));

            for (int row = 0; row <= rows; row++) {
                g2.drawLine(startX, startY + row * cell, startX + totalW, startY + row * cell);
            }

            for (int col = 0; col <= cols; col++) {
                g2.drawLine(startX + col * cell, startY, startX + col * cell, startY + totalH);
            }

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    Color color = letterModel.getCell(row, col);

                    if (color != null && !Color.WHITE.equals(color)) {
                        g2.setColor(color);
                        g2.fillRect(
                                startX + col * cell + 1,
                                startY + row * cell + 1,
                                cell - 1,
                                cell - 1
                        );
                    }
                }
            }

            g2.dispose();
        }
    }
}
