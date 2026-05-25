import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;


public class EmbroideryEditorScreen extends JFrame {

    private static final int ROWS = 52;
    private static final int COLS = 50;

    private final Color[][] cells = new Color[ROWS][COLS];
    private final DrawingPanel drawingPanel;

    private Color selectedColor = new Color(190, 0, 0);
    private boolean eraserMode = false;
    private boolean showGrid = true;

    private JCheckBox noSymmetryBox;
    private JCheckBox horizontalBox;
    private JCheckBox verticalBox;
    private JCheckBox hideGridBox;

    public EmbroideryEditorScreen() {
        super("Редактор схем вишивки — Попкова Кристина");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1400, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createHelpMenu();

        drawingPanel = new DrawingPanel();

        add(createTopPanel(), BorderLayout.NORTH);
        add(createLeftPanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);

        setVisible(true);
    }

    private void createHelpMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");

        JMenu helpMenu = new JMenu("Довідка");
        JMenuItem openHelpItem = new JMenuItem("Відкрити довідку");

        openHelpItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(openHelpItem);

        helpMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showHelpDialog();
            }
        });

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JPanel createTopPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setBackground(Color.WHITE);

        top.add(createTopButton("Сітка: 50 x 52", e -> JOptionPane.showMessageDialog(this, "Розмір сітки: 50 x 52")));
        top.add(createTopButton("-", e -> drawingPanel.zoomOut()));
        top.add(createTopButton("+", e -> drawingPanel.zoomIn()));
        top.add(createTopButton("Текст → орнамент", e -> createTextOrnament()));
        top.add(createTopButton("Зберегти PNG", e -> savePng()));
        top.add(createTopButton("Відкрити PNG/JPG", e -> openImage()));
        top.add(createTopButton("Зберегти в історію", e -> JOptionPane.showMessageDialog(this, "Орнамент збережено в історію")));
        top.add(createTopButton("Історія", e -> JOptionPane.showMessageDialog(this, "Тут буде історія збережених орнаментів")));
        top.add(createTopButton("До меню", e -> dispose()));

        return top;
    }

    private JButton createTopButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(170, 205, 235), 2));
        btn.setPreferredSize(new Dimension(155, 38));
        btn.addActionListener(listener);
        return btn;
    }

    private JPanel createLeftPanel() {
        JPanel left = new JPanel();
        left.setPreferredSize(new Dimension(330, 700));
        left.setBackground(Color.WHITE);
        left.setLayout(new BorderLayout());

        JLabel title = new JLabel("Абетка та орнаменти", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setForeground(new Color(125, 55, 50));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
        left.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createLineBorder(new Color(170, 205, 235), 2));

        JLabel ornamentsTitle = new JLabel("Орнаменти", SwingConstants.CENTER);
        ornamentsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        ornamentsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        ornamentsTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        content.add(ornamentsTitle);

        JPanel ornaments = new JPanel(new GridLayout(2, 3, 10, 10));
        ornaments.setBackground(Color.WHITE);
        ornaments.setMaximumSize(new Dimension(260, 110));
        String[] ornamentNames = {"Піксель", "Ромб", "Хрест", "Дерево", "Зірка", "Калина"};
        for (String name : ornamentNames) {
            ornaments.add(createSmallButton(name, e -> drawSimpleOrnament(name)));
        }
        content.add(ornaments);

        JLabel lettersTitle = new JLabel("Українська абетка", SwingConstants.CENTER);
        lettersTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lettersTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lettersTitle.setBorder(BorderFactory.createEmptyBorder(25, 0, 15, 0));
        content.add(lettersTitle);

        JPanel letters = new JPanel(new GridLayout(7, 5, 8, 8));
        letters.setBackground(Color.WHITE);
        letters.setMaximumSize(new Dimension(250, 360));

        String[] alphabet = {
                "А", "Б", "В", "Г", "Ґ",
                "Д", "Е", "Є", "Ж", "З",
                "И", "І", "Ї", "Й", "К",
                "Л", "М", "Н", "О", "П",
                "Р", "С", "Т", "У", "Ф",
                "Х", "Ц", "Ч", "Ш", "Щ",
                "Ь", "Ю", "Я"
        };

        for (String letter : alphabet) {
            JButton btn = createLetterButton(letter);
            letters.add(btn);
        }

        content.add(letters);
        left.add(content, BorderLayout.CENTER);

        return left;
    }

    private JButton createSmallButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(170, 205, 235), 2));
        btn.addActionListener(listener);
        return btn;
    }

    private JButton createLetterButton(String letter) {
        JButton btn = new JButton(letter);
        btn.setFont(new Font("Serif", Font.BOLD, 22));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(170, 205, 235), 2));
        btn.addActionListener(e -> drawLetterPattern(letter));
        return btn;
    }

    private JScrollPane createCenterPanel() {
        JScrollPane scrollPane = new JScrollPane(drawingPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(170, 205, 235), 2));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JPanel createRightPanel() {
        JPanel right = new JPanel();
        right.setPreferredSize(new Dimension(310, 700));
        right.setBackground(Color.WHITE);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel paletteTitle = new JLabel("Палітра кольорів");
        paletteTitle.setFont(new Font("Arial", Font.BOLD, 20));
        paletteTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(paletteTitle);

        JPanel palette = new JPanel(new GridLayout(8, 8, 4, 4));
        palette.setBackground(Color.WHITE);
        palette.setMaximumSize(new Dimension(270, 250));
        palette.setAlignmentX(Component.LEFT_ALIGNMENT);

        Color[] colors = {
                Color.RED, new Color(180, 0, 0), new Color(255, 45, 45), new Color(255, 90, 90),
                new Color(255, 130, 130), new Color(255, 170, 170), new Color(255, 205, 205), new Color(245, 235, 235),

                new Color(130, 60, 0), new Color(180, 90, 0), new Color(220, 130, 0), new Color(255, 180, 0),
                new Color(255, 220, 0), new Color(255, 240, 80), new Color(210, 240, 80), new Color(120, 220, 60),

                new Color(0, 80, 70), new Color(0, 120, 90), new Color(0, 150, 100), new Color(40, 190, 130),
                new Color(90, 220, 160), new Color(140, 235, 180), new Color(180, 245, 210), new Color(210, 250, 230),

                new Color(0, 60, 120), new Color(0, 90, 150), new Color(0, 130, 180), new Color(40, 160, 200),
                new Color(80, 190, 220), new Color(120, 210, 230), new Color(170, 230, 240), new Color(210, 245, 250),

                new Color(40, 30, 150), new Color(70, 50, 190), new Color(100, 70, 220), new Color(130, 90, 235),
                new Color(160, 110, 240), new Color(190, 140, 245), new Color(220, 170, 245), new Color(240, 210, 250),

                new Color(140, 0, 100), new Color(180, 0, 140), new Color(210, 0, 180), new Color(230, 40, 200),
                new Color(240, 80, 215), new Color(245, 130, 225), new Color(250, 180, 235), new Color(255, 220, 245),

                Color.BLACK, new Color(30, 40, 50), new Color(60, 70, 80), new Color(90, 100, 110),
                new Color(120, 130, 140), new Color(160, 170, 180), new Color(200, 215, 230), Color.WHITE,

                new Color(80, 45, 20), new Color(120, 75, 35), new Color(160, 105, 55), new Color(200, 145, 80),
                new Color(230, 180, 110), new Color(245, 210, 160), new Color(245, 230, 200), new Color(240, 240, 230)
        };

        for (Color color : colors) {
            JButton colorBtn = new JButton();
            colorBtn.setBackground(color);
            colorBtn.setFocusPainted(false);
            colorBtn.setBorder(BorderFactory.createLineBorder(new Color(170, 205, 235), 1));
            colorBtn.addActionListener(e -> {
                selectedColor = color;
                eraserMode = false;
            });
            palette.add(colorBtn);
        }

        right.add(Box.createVerticalStrut(10));
        right.add(palette);

        JButton chooseColor = createRightButton("Обрати колір...");
        chooseColor.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Оберіть колір", selectedColor);
            if (chosen != null) {
                selectedColor = chosen;
                eraserMode = false;
            }
        });
        right.add(Box.createVerticalStrut(10));
        right.add(chooseColor);

        JLabel toolsTitle = new JLabel("Інструменти");
        toolsTitle.setFont(new Font("Arial", Font.BOLD, 20));
        toolsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolsTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 8, 0));
        right.add(toolsTitle);

        JPanel tools = new JPanel(new GridLayout(4, 2, 8, 8));
        tools.setBackground(Color.WHITE);
        tools.setMaximumSize(new Dimension(280, 190));
        tools.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton brush = createRightButton("Пензлик");
        brush.addActionListener(e -> eraserMode = false);

        JButton eraser = createRightButton("Ластик");
        eraser.addActionListener(e -> eraserMode = true);

        JButton clear = createRightButton("Стерти все");
        clear.addActionListener(e -> clearCanvas());

        JButton duplicateRight = createRightButton("Дубль →");
        duplicateRight.addActionListener(e -> duplicateRight());

        JButton duplicateDown = createRightButton("Дубль ↓");
        duplicateDown.addActionListener(e -> duplicateDown());

        JButton back = createRightButton("Назад");
        back.addActionListener(e -> JOptionPane.showMessageDialog(this, "Дія назад поки не налаштована"));

        tools.add(brush);
        tools.add(eraser);
        tools.add(clear);
        tools.add(duplicateRight);
        tools.add(duplicateDown);
        tools.add(back);

        right.add(tools);

        JLabel autoTitle = new JLabel("Автоматизація");
        autoTitle.setFont(new Font("Arial", Font.BOLD, 20));
        autoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 8, 0));
        right.add(autoTitle);

        horizontalBox = new JCheckBox("Горизонтальна вісь");
        verticalBox = new JCheckBox("Вертикальна вісь");
        noSymmetryBox = new JCheckBox("Без симетрії");
        hideGridBox = new JCheckBox("Прибрати сітку");

        noSymmetryBox.setSelected(true);

        setupCheckBox(horizontalBox);
        setupCheckBox(verticalBox);
        setupCheckBox(noSymmetryBox);
        setupCheckBox(hideGridBox);

        horizontalBox.addActionListener(e -> {
            if (horizontalBox.isSelected()) noSymmetryBox.setSelected(false);
        });

        verticalBox.addActionListener(e -> {
            if (verticalBox.isSelected()) noSymmetryBox.setSelected(false);
        });

        noSymmetryBox.addActionListener(e -> {
            if (noSymmetryBox.isSelected()) {
                horizontalBox.setSelected(false);
                verticalBox.setSelected(false);
            }
        });

        hideGridBox.addActionListener(e -> {
            showGrid = !hideGridBox.isSelected();
            drawingPanel.repaint();
        });

        right.add(horizontalBox);
        right.add(verticalBox);
        right.add(noSymmetryBox);
        right.add(hideGridBox);

        return right;
    }

    private JButton createRightButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(170, 205, 235), 2));
        btn.setMaximumSize(new Dimension(280, 38));
        return btn;
    }

    private void setupCheckBox(JCheckBox box) {
        box.setBackground(Color.WHITE);
        box.setFont(new Font("Arial", Font.BOLD, 14));
        box.setFocusPainted(false);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void showHelpDialog() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 15));
        textArea.setMargin(new Insets(15, 15, 15, 15));
        textArea.setBackground(new Color(250, 250, 250));

        textArea.setText("""
                РЕДАКТОР СХЕМ ВИШИВКИ

                Ця програма призначена для створення схем української вишивки на клітинковому полотні.

                ОСНОВНІ МОЖЛИВОСТІ:

                1. Малювання орнаментів
                Натискайте на клітинки полотна, щоб створювати власний орнамент.

                2. Вибір кольору
                Праворуч розташована палітра кольорів. Натисніть на потрібний колір, щоб малювати ним.

                3. Інструменти
                Пензлик — малює клітинки вибраним кольором.
                Ластик — очищає клітинки.
                Стерти все — повністю очищає полотно.
                Дубль → — дублює орнамент вправо.
                Дубль ↓ — дублює орнамент вниз.
                Назад — повертає попередню дію.

                4. Текст → орнамент
                Ця кнопка дозволяє створити орнамент із введеного імені або слова.

                5. Українська абетка
                Ліворуч розташовані літери української абетки.
                Кожна літера має власний орнамент, який можна додати на полотно.

                6. Автоматизація
                Горизонтальна вісь — симетрія по горизонталі.
                Вертикальна вісь — симетрія по вертикалі.
                Без симетрії — звичайне малювання.
                Прибрати сітку — приховує лінії полотна.

                7. Збереження
                Зберегти PNG — зберігає поточний орнамент як зображення.
                Відкрити PNG/JPG — відкриває готове зображення.
                Зберегти в історію — додає створений орнамент до історії.
                Історія — відкриває збережені роботи.

                8. Масштабування
                Кнопки + і - змінюють розмір клітинок полотна.

                9. До меню
                Кнопка повертає користувача до головного меню програми.
                """);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 480));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Довідка",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void paintCell(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;

        Color color = eraserMode ? null : selectedColor;
        cells[row][col] = color;

        if (horizontalBox != null && horizontalBox.isSelected()) {
            int mirrorRow = ROWS - 1 - row;
            cells[mirrorRow][col] = color;
        }

        if (verticalBox != null && verticalBox.isSelected()) {
            int mirrorCol = COLS - 1 - col;
            cells[row][mirrorCol] = color;
        }

        if (horizontalBox != null && verticalBox != null && horizontalBox.isSelected() && verticalBox.isSelected()) {
            int mirrorRow = ROWS - 1 - row;
            int mirrorCol = COLS - 1 - col;
            cells[mirrorRow][mirrorCol] = color;
        }

        drawingPanel.repaint();
    }

    private void clearCanvas() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                cells[r][c] = null;
            }
        }
        drawingPanel.repaint();
    }

    private void duplicateRight() {
        int shift = 5;

        for (int r = 0; r < ROWS; r++) {
            for (int c = COLS - shift - 1; c >= 0; c--) {
                if (cells[r][c] != null) {
                    cells[r][c + shift] = cells[r][c];
                }
            }
        }

        drawingPanel.repaint();
    }

    private void duplicateDown() {
        int shift = 5;

        for (int r = ROWS - shift - 1; r >= 0; r--) {
            for (int c = 0; c < COLS; c++) {
                if (cells[r][c] != null) {
                    cells[r + shift][c] = cells[r][c];
                }
            }
        }

        drawingPanel.repaint();
    }

    private void drawSimpleOrnament(String name) {
        clearCanvas();

        int centerR = ROWS / 2;
        int centerC = COLS / 2;

        if (name.equals("Ромб")) {
            for (int i = 0; i < 12; i++) {
                setCell(centerR - i, centerC + i, selectedColor);
                setCell(centerR + i, centerC + i, selectedColor);
                setCell(centerR - i, centerC - i, selectedColor);
                setCell(centerR + i, centerC - i, selectedColor);
            }
        } else if (name.equals("Хрест")) {
            for (int i = -12; i <= 12; i++) {
                setCell(centerR, centerC + i, selectedColor);
                setCell(centerR + i, centerC, selectedColor);
            }
        } else if (name.equals("Зірка")) {
            for (int i = -10; i <= 10; i++) {
                setCell(centerR, centerC + i, selectedColor);
                setCell(centerR + i, centerC, selectedColor);
                setCell(centerR + i, centerC + i, selectedColor);
                setCell(centerR + i, centerC - i, selectedColor);
            }
        } else if (name.equals("Дерево")) {
            for (int i = 0; i < 15; i++) {
                setCell(centerR + i, centerC, selectedColor);
            }
            for (int i = 0; i < 8; i++) {
                setCell(centerR + 4 - i, centerC - i, selectedColor);
                setCell(centerR + 4 - i, centerC + i, selectedColor);
                setCell(centerR + 9 - i, centerC - i, selectedColor);
                setCell(centerR + 9 - i, centerC + i, selectedColor);
            }
        } else if (name.equals("Калина")) {
            for (int i = 0; i < 5; i++) {
                setCell(centerR, centerC + i, Color.RED);
                setCell(centerR + 1, centerC + i, Color.RED);
                setCell(centerR - 1, centerC + i, Color.RED);
            }
            setCell(centerR - 2, centerC - 2, Color.RED);
            setCell(centerR + 2, centerC - 2, Color.RED);
            setCell(centerR - 2, centerC + 6, Color.RED);
            setCell(centerR + 2, centerC + 6, Color.RED);
        } else {
            setCell(centerR, centerC, selectedColor);
            setCell(centerR, centerC + 1, selectedColor);
            setCell(centerR + 1, centerC, selectedColor);
            setCell(centerR + 1, centerC + 1, selectedColor);
        }

        drawingPanel.repaint();
    }

    private void drawLetterPattern(String letter) {
        clearCanvas();

        int startR = 18;
        int startC = 20;

        int code = letter.charAt(0);

        for (int i = 0; i < 7; i++) {
            setCell(startR + i, startC, selectedColor);
            setCell(startR + i, startC + 4, selectedColor);
        }

        for (int i = 0; i < 5; i++) {
            setCell(startR, startC + i, selectedColor);
            setCell(startR + 3, startC + i, selectedColor);
        }

        if (code % 2 == 0) {
            for (int i = 0; i < 5; i++) {
                setCell(startR + 6, startC + i, selectedColor);
            }
        }

        if (code % 3 == 0) {
            for (int i = 0; i < 7; i++) {
                setCell(startR + i, startC + i % 5, Color.BLACK);
            }
        }

        drawingPanel.repaint();
    }

    private void createTextOrnament() {
        String text = JOptionPane.showInputDialog(this, "Введіть ім’я або слово:");
        if (text == null || text.trim().isEmpty()) return;

        clearCanvas();

        text = text.trim().toUpperCase();

        int startR = 18;
        int startC = 4;

        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            int baseC = startC + index * 7;

            if (baseC + 5 >= COLS) break;

            for (int i = 0; i < 7; i++) {
                setCell(startR + i, baseC, selectedColor);
                setCell(startR + i, baseC + 4, selectedColor);
            }

            for (int i = 0; i < 5; i++) {
                setCell(startR, baseC + i, selectedColor);
                setCell(startR + 3, baseC + i, selectedColor);
            }

            if (ch % 2 == 0) {
                for (int i = 0; i < 5; i++) {
                    setCell(startR + 6, baseC + i, Color.BLACK);
                }
            }

            if (ch % 3 == 0) {
                for (int i = 0; i < 7; i++) {
                    setCell(startR + i, baseC + i % 5, Color.BLACK);
                }
            }
        }

        drawingPanel.repaint();
    }

    private void setCell(int row, int col, Color color) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            cells[row][col] = color;
        }
    }

    private void savePng() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("ornament.png"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            BufferedImage image = drawingPanel.createImageFromGrid();
            ImageIO.write(image, "png", chooser.getSelectedFile());
            JOptionPane.showMessageDialog(this, "PNG збережено успішно!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Помилка збереження PNG: " + ex.getMessage());
        }
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            BufferedImage image = ImageIO.read(chooser.getSelectedFile());
            if (image == null) {
                JOptionPane.showMessageDialog(this, "Не вдалося відкрити зображення.");
                return;
            }

            clearCanvas();

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int x = c * image.getWidth() / COLS;
                    int y = r * image.getHeight() / ROWS;
                    Color color = new Color(image.getRGB(x, y), true);

                    if (color.getAlpha() > 0 && !(color.getRed() > 240 && color.getGreen() > 240 && color.getBlue() > 240)) {
                        cells[r][c] = color;
                    }
                }
            }

            drawingPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Помилка відкриття зображення: " + ex.getMessage());
        }
    }

    private class DrawingPanel extends JPanel {
        private int cellSize = 16;

        public DrawingPanel() {
            setPreferredSize(new Dimension(COLS * cellSize, ROWS * cellSize));
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMouse(e);
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    handleMouse(e);
                }
            });
        }

        private void handleMouse(MouseEvent e) {
            int col = e.getX() / cellSize;
            int row = e.getY() / cellSize;
            paintCell(row, col);
        }

        public void zoomIn() {
            cellSize += 2;
            updateSize();
        }

        public void zoomOut() {
            if (cellSize > 6) {
                cellSize -= 2;
                updateSize();
            }
        }

        private void updateSize() {
            setPreferredSize(new Dimension(COLS * cellSize, ROWS * cellSize));
            revalidate();
            repaint();
        }

        public BufferedImage createImageFromGrid() {
            BufferedImage image = new BufferedImage(COLS * cellSize, ROWS * cellSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (cells[r][c] != null) {
                        g2.setColor(cells[r][c]);
                        g2.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                    }
                }
            }

            g2.dispose();
            return image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (cells[r][c] != null) {
                        g2.setColor(cells[r][c]);
                        g2.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                    }
                }
            }

            if (showGrid) {
                g2.setColor(new Color(210, 210, 210));

                for (int r = 0; r <= ROWS; r++) {
                    g2.drawLine(0, r * cellSize, COLS * cellSize, r * cellSize);
                }

                for (int c = 0; c <= COLS; c++) {
                    g2.drawLine(c * cellSize, 0, c * cellSize, ROWS * cellSize);
                }
            }
        }
    }
}