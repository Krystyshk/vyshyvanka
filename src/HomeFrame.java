import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class HomeFrame extends JFrame {
    private final int WINDOW_W = 1250;
    private final int WINDOW_H = 807;

    private final Color bgColor = new Color(245, 241, 232);
    private final Color textRed = new Color(125, 55, 50);
    private final Color headerBlue = new Color(226, 241, 252);
    private final Color stripRed = new Color(148, 54, 48);

    public HomeFrame() {
        super("Вишиванка власноруч — Попкова Кристина. ІПЗ-1");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AppWindow.setup(this);

        showHomeScreen();
    }

    private void showHomeScreen() {
        setContentPane(new HomePanel());
        revalidate();
        repaint();
    }

    private void showOrnamentsScreen() {
        AppWindow.open(this, new OrnamentsFrame());
    }

    private void showHistoryScreen() {
        AppWindow.open(this, new HistoryFrame());
    }

    private void openConstructor() {
        AppWindow.open(this, new EmbroideryFrame());
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource(path));
            BufferedImage cropped = cropWhiteOrTransparent(image);
            Image scaled = cropped.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
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
        ImageIcon icon = loadIcon(path, w, h);

        if (icon != null) {
            label.setIcon(icon);
        }

        return label;
    }

    private BufferedImage cropWhiteOrTransparent(BufferedImage image) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = 0;
        int maxY = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);

                int a = (argb >> 24) & 255;
                int r = (argb >> 16) & 255;
                int g = (argb >> 8) & 255;
                int b = argb & 255;

                boolean visible = a > 20;
                boolean notWhite = !(r > 245 && g > 245 && b > 245);
                boolean notGreen = !(g > 70 && r < 100 && b < 100);
                boolean notBlack = !(r < 15 && g < 15 && b < 15);

                if (visible && notWhite && notGreen && notBlack) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX <= minX || maxY <= minY) {
            return image;
        }

        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    // Кнопка у стилі вишитої рамки
    private class StitchButton extends JButton {
        public StitchButton(String text) {
            super(text);

            setFont(new Font("Arial", Font.BOLD, 14));
            setForeground(textRed);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(4, 5, w - 8, h - 8, 18, 18);

            g2.setColor(new Color(255, 253, 248));
            g2.fillRoundRect(2, 2, w - 6, h - 7, 18, 18);

            g2.setColor(new Color(185, 188, 210));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(2, 2, w - 6, h - 7, 18, 18);

            g2.setColor(new Color(170, 170, 195));
            g2.setStroke(new BasicStroke(
                    2,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    0,
                    new float[]{7, 6},
                    0
            ));
            g2.drawRoundRect(10, 9, w - 22, h - 21, 14, 14);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // Картка розділу з кнопкою всередині
    private class SectionCard extends JPanel {
        public SectionCard(String imagePath, String buttonText, Runnable action, int cardW, int cardH) {
            setLayout(null);
            setOpaque(false);

            JLabel image = new JLabel(loadIconNoCrop(imagePath, cardW, cardH));
            image.setBounds(0, 0, cardW, cardH);
            add(image);

            JButton button = new StitchButton(buttonText);

            int buttonW = cardW - 64;
            int buttonH = 44;
            int buttonX = 32;
            int buttonY = cardH - 60;

            button.setBounds(buttonX, buttonY, buttonW, buttonH);
            button.addActionListener(e -> action.run());
            add(button);

            setComponentZOrder(button, 0);
            setComponentZOrder(image, 1);
        }
    }

    // Кнопка з літерою
    private class LetterButton extends JButton {
        public LetterButton(String text) {
            super(text);

            setFont(new Font("Georgia", Font.BOLD, 24));
            setForeground(new Color(45, 80, 90));
            setBackground(new Color(255, 253, 248));
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(javax.swing.BorderFactory.createLineBorder(new Color(170, 190, 220), 2));
        }
    }

    // Великий блок перегляду піксельної вишивки
    private class PixelPreviewPanel extends JPanel {
        private char selectedLetter = 'А';

        public PixelPreviewPanel() {
            setBackground(new Color(255, 253, 248, 235));
            setBorder(javax.swing.BorderFactory.createLineBorder(new Color(170, 190, 220), 3));
        }

        public void setSelectedLetter(char selectedLetter) {
            this.selectedLetter = selectedLetter;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Georgia", Font.BOLD, 30));
            g2.setColor(textRed);
            g2.drawString("Обрана літера: " + selectedLetter, 35, 50);

            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            g2.setColor(Color.BLACK);
            g2.drawString("Піксельний орнамент для цієї літери", 35, 82);

            g2.setFont(new Font("Georgia", Font.BOLD, 54));
            g2.setColor(new Color(45, 80, 90));
            g2.drawString(String.valueOf(selectedLetter), 185, 145);

            int[][] pattern = getEmbroideryPattern(selectedLetter);

            int cell = 18;
            int patternW = pattern[0].length * cell;
            int patternH = pattern.length * cell;

            int startX = (getWidth() - patternW) / 2;
            int startY = 175;

            g2.setColor(new Color(255, 255, 255, 245));
            g2.fillRect(startX - 14, startY - 14, patternW + 28, patternH + 28);

            for (int y = 0; y < pattern.length; y++) {
                for (int x = 0; x < pattern[y].length; x++) {
                    int value = pattern[y][x];

                    if (value == 0) {
                        continue;
                    }

                    int px = startX + x * cell;
                    int py = startY + y * cell;

                    if (value == 1) {
                        g2.setColor(new Color(190, 35, 40));
                    } else {
                        g2.setColor(new Color(35, 40, 45));
                    }

                    drawCross(g2, px, py, cell);
                }
            }

            g2.dispose();
        }

        private void drawCross(Graphics2D g2, int x, int y, int cell) {
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int pad = 4;

            g2.drawLine(x + pad, y + pad, x + cell - pad, y + cell - pad);
            g2.drawLine(x + cell - pad, y + pad, x + pad, y + cell - pad);
        }

        private int[][] getEmbroideryPattern(char letter) {
            switch (letter) {
                case 'А':
                    return p(
                            "000010000",
                            "000101000",
                            "001000100",
                            "010000010",
                            "001000100",
                            "000101000",
                            "000010000"
                    );

                case 'Б':
                    return p(
                            "001010100",
                            "010101010",
                            "100000001",
                            "010000010",
                            "001000100",
                            "010101010",
                            "001010100"
                    );

                case 'В':
                    return p(
                            "000010000",
                            "000010000",
                            "001111100",
                            "010010010",
                            "001111100",
                            "000010000",
                            "000010000"
                    );

                case 'Г':
                    return p(
                            "000010000",
                            "000111000",
                            "001111100",
                            "011111110",
                            "001111100",
                            "000111000",
                            "000010000"
                    );

                case 'Ґ':
                    return p(
                            "000010000",
                            "000111000",
                            "001010100",
                            "011111110",
                            "001010100",
                            "000111000",
                            "000010000"
                    );

                case 'Д':
                    return p(
                            "200000002",
                            "020000020",
                            "002010200",
                            "000111000",
                            "002010200",
                            "020000020",
                            "200000002"
                    );

                case 'Е':
                    return p(
                            "020000020",
                            "202000202",
                            "020101020",
                            "001111100",
                            "020101020",
                            "202000202",
                            "020000020"
                    );

                case 'Є':
                    return p(
                            "020000020",
                            "202000202",
                            "000101000",
                            "011111110",
                            "000101000",
                            "202000202",
                            "020000020"
                    );

                case 'Ж':
                    return p(
                            "202000202",
                            "020202020",
                            "002111200",
                            "001111100",
                            "002111200",
                            "020202020",
                            "202000202"
                    );

                case 'З':
                    return p(
                            "222000222",
                            "222000222",
                            "222101222",
                            "000111000",
                            "222101222",
                            "222000222",
                            "222000222"
                    );

                case 'И':
                    return p(
                            "000010000",
                            "000101000",
                            "010010010",
                            "101000101",
                            "010010010",
                            "000101000",
                            "000010000"
                    );

                case 'І':
                    return p(
                            "000000000",
                            "000010000",
                            "000101000",
                            "001111100",
                            "000101000",
                            "000010000",
                            "000000000"
                    );

                case 'Ї':
                    return p(
                            "001000100",
                            "000000000",
                            "000010000",
                            "001111100",
                            "000010000",
                            "000000000",
                            "001000100"
                    );

                case 'Й':
                    return p(
                            "000101000",
                            "000000000",
                            "001000100",
                            "010101010",
                            "001000100",
                            "000000000",
                            "000101000"
                    );

                case 'К':
                    return p(
                            "001111100",
                            "010111010",
                            "101111101",
                            "011111110",
                            "101111101",
                            "010111010",
                            "001111100"
                    );

                case 'Л':
                    return p(
                            "000010000",
                            "000101000",
                            "001000100",
                            "010000010",
                            "001000100",
                            "000101000",
                            "000010000"
                    );

                case 'М':
                    return p(
                            "001000100",
                            "010101010",
                            "101000101",
                            "010000010",
                            "101000101",
                            "010101010",
                            "001000100"
                    );

                case 'Н':
                    return p(
                            "000010000",
                            "000111000",
                            "001111100",
                            "011111110",
                            "001111100",
                            "000111000",
                            "000010000"
                    );

                case 'О':
                    return p(
                            "000111000",
                            "001111100",
                            "011111110",
                            "111111111",
                            "011111110",
                            "001111100",
                            "000111000"
                    );

                case 'П':
                    return p(
                            "000111000",
                            "001111100",
                            "011101110",
                            "111000111",
                            "011101110",
                            "001111100",
                            "000111000"
                    );

                case 'Р':
                    return p(
                            "000100000",
                            "001010000",
                            "010001000",
                            "100000100",
                            "010001000",
                            "001010000",
                            "000100000"
                    );

                case 'С':
                    return p(
                            "000010000",
                            "000101000",
                            "001000100",
                            "010101010",
                            "001000100",
                            "000101000",
                            "000010000"
                    );

                case 'Т':
                    return p(
                            "001110000",
                            "010001000",
                            "100000100",
                            "010001000",
                            "001110000"
                    );

                case 'У':
                    return p(
                            "100010001",
                            "010101010",
                            "001000100",
                            "000101000",
                            "001000100",
                            "010101010",
                            "100010001"
                    );

                case 'Ф':
                    return p(
                            "000010000",
                            "001111100",
                            "010101010",
                            "101000101",
                            "010101010",
                            "001111100",
                            "000010000"
                    );

                case 'Х':
                    return p(
                            "000010000",
                            "000111000",
                            "001111100",
                            "011111110",
                            "001111100",
                            "000111000",
                            "000010000"
                    );

                case 'Ц':
                    return p(
                            "000010000",
                            "000111000",
                            "001111100",
                            "011111110",
                            "001111100",
                            "000111000",
                            "000010000"
                    );

                case 'Ч':
                    return p(
                            "001000100",
                            "010101010",
                            "100010001",
                            "010000010",
                            "001000100",
                            "010101010",
                            "001000100"
                    );

                case 'Ш':
                    return p(
                            "100010001",
                            "010101010",
                            "001000100",
                            "000000000",
                            "001000100",
                            "010101010",
                            "100010001"
                    );

                case 'Щ':
                    return p(
                            "100000001",
                            "010101010",
                            "001000100",
                            "000000000",
                            "001000100",
                            "010101010",
                            "100000001"
                    );

                case 'Ь':
                    return p(
                            "000010000",
                            "001111100",
                            "010111010",
                            "001111100",
                            "000010000"
                    );

                case 'Ю':
                    return p(
                            "000010000",
                            "001111100",
                            "010101010",
                            "101000101",
                            "010101010",
                            "001111100",
                            "000010000"
                    );

                case 'Я':
                    return p(
                            "000010000",
                            "000101000",
                            "001000100",
                            "010000010",
                            "001000100",
                            "000101000",
                            "000010000"
                    );

                default:
                    return p(
                            "000010000",
                            "000101000",
                            "001000100",
                            "000101000",
                            "000010000"
                    );
            }
        }

        private int[][] p(String... rows) {
            int[][] result = new int[rows.length][rows[0].length()];

            for (int y = 0; y < rows.length; y++) {
                for (int x = 0; x < rows[y].length(); x++) {
                    result[y][x] = rows[y].charAt(x) - '0';
                }
            }

            return result;
        }
    }

    private class HomePanel extends JPanel {
        private final BufferedImage bg = loadImage("/images/home_bg.jpeg");

        public HomePanel() {
            setLayout(null);

            JLabel logo = imageLabel("/images/logo_big.jpeg", 290, 76);
            logo.setBounds(45, 20, 290, 76);
            add(logo);

            JLabel topText = new JLabel("Розроби вишиванку, яку відчуваєш!");
            topText.setFont(new Font("Georgia", Font.BOLD, 34));
            topText.setForeground(textRed);
            topText.setBounds(450, 32, 780, 55);
            add(topText);

            JLabel description = new JLabel(
                    "<html>Дізнайся більше про історію саме своєї вишивки,<br>" +
                            "роздивись які бувають орнаменти та зроби свою<br>" +
                            "власну вишиванку!</html>"
            );
            description.setFont(new Font("Arial", Font.BOLD, 22));
            description.setForeground(Color.BLACK);
            description.setBounds(75, 255, 610, 105);
            add(description);

            JLabel vyshyvanky = imageLabel("/images/vyshyvanky.png", 420, 370);
            vyshyvanky.setBounds(745, 135, 420, 370);
            add(vyshyvanky);

            int cardW = 320;
            int cardH = 220;
            int cardY = 525;

            SectionCard constructorCard = new SectionCard(
                    "/images/card_constructor.jpeg",
                    "Відкрити конструктор →",
                    () -> openConstructor(),
                    cardW,
                    cardH
            );
            constructorCard.setBounds(75, cardY, cardW, cardH);
            add(constructorCard);

            SectionCard ornamentsCard = new SectionCard(
                    "/images/card_ornaments.jpeg",
                    "До орнаментів →",
                    () -> showOrnamentsScreen(),
                    cardW,
                    cardH
            );
            ornamentsCard.setBounds(465, cardY, cardW, cardH);
            add(ornamentsCard);

            SectionCard historyCard = new SectionCard(
                    "/images/card_history.jpeg",
                    "Відкрити історію →",
                    () -> showHistoryScreen(),
                    cardW,
                    cardH
            );
            historyCard.setBounds(855, cardY, cardW, cardH);
            add(historyCard);

            setComponentZOrder(constructorCard, 0);
            setComponentZOrder(ornamentsCard, 0);
            setComponentZOrder(historyCard, 0);
            setComponentZOrder(vyshyvanky, getComponentCount() - 1);

            setComponentZOrder(logo, 0);
            setComponentZOrder(topText, 0);
            setComponentZOrder(description, 0);
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
            g.fillRect(0, 0, getWidth(), 115);

            g.setColor(new Color(180, 210, 235));
            g.drawLine(0, 115, getWidth(), 115);

            g.setColor(stripRed);
            g.fillRoundRect(70, 235, 610, 8, 8, 8);

            g.setColor(Color.WHITE);
            g.fillRoundRect(70, 375, 610, 10, 10, 10);

            g.setColor(new Color(210, 225, 240));
            g.drawRoundRect(70, 375, 610, 10, 10, 10);

            int stripY = 485;
            int stripH = 275;

            g.setColor(Color.WHITE);
            g.fillRect(0, stripY, getWidth(), stripH);

            g.setColor(stripRed);
            g.fillRect(0, stripY, getWidth(), 7);
        }
    }

    private class OrnamentsPanel extends JPanel {
        private final BufferedImage bg = loadImage("/images/home_bg.jpeg");

        public OrnamentsPanel() {
            setLayout(null);

            JLabel logo = imageLabel("/images/logo_big.jpeg", 230, 60);
            logo.setBounds(70, 35, 230, 60);
            add(logo);

            JLabel topText = new JLabel("Орнаменти за буквами");
            topText.setFont(new Font("Georgia", Font.BOLD, 38));
            topText.setForeground(textRed);
            topText.setHorizontalAlignment(JLabel.CENTER);
            topText.setBounds(0, 50, WINDOW_W, 60);
            add(topText);

            int rightX = 990;
            int rightW = 200;

            JButton homeButton = new StitchButton("На головну →");
            homeButton.setBounds(rightX, 25, rightW, 42);
            homeButton.addActionListener(e -> showHomeScreen());
            add(homeButton);

            JButton constructorButton = new StitchButton("Конструктор →");
            constructorButton.setBounds(rightX, 75, rightW, 42);
            constructorButton.addActionListener(e -> openConstructor());
            add(constructorButton);

            JButton historyButton = new StitchButton("Історія →");
            historyButton.setBounds(rightX, 125, rightW, 42);
            historyButton.addActionListener(e -> showHistoryScreen());
            add(historyButton);

            JPanel infoBlock = new JPanel(null);
            infoBlock.setBackground(new Color(255, 255, 255, 220));
            infoBlock.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(170, 190, 220), 2));
            infoBlock.setBounds(70, 205, 720, 125);
            add(infoBlock);

            JLabel infoTitle = new JLabel("Український алфавіт у вишивці");
            infoTitle.setFont(new Font("Georgia", Font.BOLD, 26));
            infoTitle.setForeground(textRed);
            infoTitle.setBounds(20, 12, 650, 35);
            infoBlock.add(infoTitle);

            JLabel infoText = new JLabel(
                    "<html>Натисни на літеру, щоб побачити її піксельний орнамент. " +
                            "Обрана літера перетворюється на маленьку піксельну схему, " +
                            "яку можна перенести у конструктор орнаменту для створення іменного візерунка.</html>"
            );
            infoText.setFont(new Font("Arial", Font.PLAIN, 18));
            infoText.setForeground(Color.BLACK);
            infoText.setBounds(20, 50, 670, 65);
            infoBlock.add(infoText);

            PixelPreviewPanel previewPanel = new PixelPreviewPanel();
            previewPanel.setBounds(820, 205, 370, 420);
            add(previewPanel);

            JLabel alphabetTitle = new JLabel("Оберіть літеру");
            alphabetTitle.setFont(new Font("Georgia", Font.BOLD, 30));
            alphabetTitle.setForeground(textRed);
            alphabetTitle.setBounds(70, 380, 400, 45);
            add(alphabetTitle);

            JPanel alphabetPanel = new JPanel(new java.awt.GridLayout(5, 7, 12, 12));
            alphabetPanel.setOpaque(false);
            alphabetPanel.setBounds(70, 435, 650, 255);

            String letters = "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ";

            for (int i = 0; i < letters.length(); i++) {
                char letter = letters.charAt(i);

                LetterButton button = new LetterButton(String.valueOf(letter));
                button.addActionListener(e -> previewPanel.setSelectedLetter(letter));

                alphabetPanel.add(button);
            }

            add(alphabetPanel);
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

    private class HistoryPanel extends JPanel {
        private final BufferedImage bg = loadImage("/images/home_bg.jpeg");

        public HistoryPanel() {
            setLayout(null);

            JLabel logo = imageLabel("/images/logo_big.jpeg", 290, 76);
            logo.setBounds(45, 20, 290, 76);
            add(logo);

            JLabel topText = new JLabel("Історія української вишивки");
            topText.setFont(new Font("Georgia", Font.BOLD, 34));
            topText.setForeground(textRed);
            topText.setBounds(520, 32, 620, 55);
            add(topText);

            JButton homeButton = new StitchButton("На головну →");
            homeButton.setBounds(1010, 38, 170, 48);
            homeButton.addActionListener(e -> showHomeScreen());
            add(homeButton);

            JLabel header = new JLabel(loadIconNoCrop("/images/card_history.jpeg", 500, 285));
            header.setBounds(375, 130, 500, 285);
            add(header);

            JLabel title = new JLabel("Історія регіонів та символів української вишивки");
            title.setFont(new Font("Georgia", Font.BOLD, 32));
            title.setForeground(textRed);
            title.setBounds(70, 420, 1000, 50);
            add(title);

            JLabel text = new JLabel(
                    "<html>" +
                            "<b>Ромб</b> — добробут, родючість та продовження роду.<br><br>" +
                            "<b>Хрест</b> — оберіг і захист від злих сил.<br><br>" +
                            "<b>Зірка</b> — гармонія, світло та життєва енергія.<br><br>" +
                            "<b>Птахи</b> — щастя, добрі новини та духовність.<br><br>" +
                            "<b>Дерево життя</b> — сила роду і зв’язок поколінь.<br><br>" +
                            "У різних регіонах України вишивка має свої кольори, композиції та орнаменти." +
                            "</html>"
            );

            text.setFont(new Font("Arial", Font.PLAIN, 22));
            text.setForeground(Color.BLACK);
            text.setBounds(90, 485, 1050, 250);
            add(text);
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
            g.fillRect(0, 0, getWidth(), 115);

            g.setColor(new Color(180, 210, 235));
            g.drawLine(0, 115, getWidth(), 115);
        }
    }
}