import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class VyshyvankaEditor extends JFrame {


    private int gridWidth = 60;
    private int gridHeight = 45;
    private int cellSize = 16;
    private double zoomLevel = 1.0;

    private Color activeColor = new Color(203, 32, 38);
    private String activeTool = "brush"; // "brush", "eraser", "stamp"
    private int[][] activeStamp = null;
    private boolean showGrid = true;
    private String displayMode = "cross";

    private Color[][] gridData;

    private final List<Color[][]> historyStack = new ArrayList<>();
    private final int MAX_HISTORY = 30;

    private EmbroideryCanvas canvasPanel;
    private JLabel sizeDisplayLabel;
    private JCheckBox symVerticalBox;
    private JCheckBox symHorizontalBox;
    private JTextArea infoArea;
    private JLabel infoLetterLabel;
    private JLabel infoTitleLabel;

    private final Color[] traditionalColors = {
            new Color(203, 32, 38),   // Красный
            Color.BLACK,              // Черный
            new Color(13, 71, 161),   // Синий
            new Color(255, 235, 59),  // Желтый
            new Color(27, 94, 32),    // Зеленый
            Color.WHITE,              // Белый
            new Color(255, 87, 34),   // Оранжевый
            new Color(233, 30, 99),   // Розовый
            new Color(156, 39, 176),  // Фиолетовый
            new Color(0, 188, 212),   // Голубой
            new Color(121, 85, 72)    // Коричневый
    };

    public VyshyvankaEditor() {
        setTitle("Редактор української вишиванки (Java Edition)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        initGrid();
        saveState();

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(18, 18, 18));

        add(createTopBar(), BorderLayout.NORTH);
        add(createLeftSidebar(), BorderLayout.WEST);
        add(createCenterWorkspace(), BorderLayout.CENTER);
        add(createRightSidebar(), BorderLayout.EAST);
    }

    private void initGrid() {
        gridData = new Color[gridHeight][gridWidth];
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                gridData[y][x] = null;
            }
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.setBackground(new Color(26, 26, 26));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 40, 40)));

        JLabel logo = new JLabel("🔱  Конструктор Вишиванки");
        logo.setFont(new Font("Philosopher", Font.BOLD, 16));
        logo.setForeground(Color.WHITE);
        topBar.add(logo);

        sizeDisplayLabel = new JLabel("Сітка: 60 × 45");
        sizeDisplayLabel.setForeground(new Color(200, 200, 200));
        sizeDisplayLabel.setFont(new Font("Arial", Font.BOLD, 12));
        topBar.add(sizeDisplayLabel);

        JButton btnSize = createStyledButton("Розмір сітки", new Color(203, 32, 38));
        btnSize.addActionListener(e -> changeSizeDialog());
        topBar.add(btnSize);

        JButton btnText = createStyledButton("Текст ➜ Орнамент", new Color(203, 32, 38));
        btnText.addActionListener(e -> openTextToOrnamentDialog());
        topBar.add(btnText);

        JButton btnSave = createStyledButton("Зберегти PNG", new Color(40, 40, 40));
        btnSave.addActionListener(e -> saveAsPNG());
        topBar.add(btnSave);

        JButton btnReset = createStyledButton("Скинути все", new Color(60, 60, 60));
        btnReset.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Очистити все полотно?", "Підтвердження", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                saveState();
                initGrid();
                canvasPanel.repaint();
            }
        });
        topBar.add(btnReset);

        return topBar;
    }

    private JPanel createLeftSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(new Color(26, 26, 26));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(40, 40, 40)));

        JLabel title = new JLabel("Абетка та символіка");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(15, 15, 10, 15));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(title);

        JPanel alphabetGrid = new JPanel(new GridLayout(7, 5, 4, 4));
        alphabetGrid.setBackground(new Color(26, 26, 26));
        alphabetGrid.setBorder(new EmptyBorder(5, 15, 15, 15));
        alphabetGrid.setMaximumSize(new Dimension(280, 220));
        alphabetGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String letter : LetterDatabase.alphabetData.keySet()) {
            JButton letterBtn = new JButton(letter);
            letterBtn.setFont(new Font("Arial", Font.BOLD, 12));
            letterBtn.setBackground(new Color(45, 45, 45));
            letterBtn.setForeground(Color.WHITE);
            letterBtn.setFocusPainted(false);
            letterBtn.setBorder(new LineBorder(new Color(60, 60, 60)));
            letterBtn.addActionListener(e -> selectLetter(letter));
            alphabetGrid.add(letterBtn);
        }
        sidebar.add(alphabetGrid);

        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(new Color(35, 35, 35));
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 15, 15, 15),
                BorderFactory.createTitledBorder(new LineBorder(new Color(203, 32, 38)), "Значення оберегу", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 11), Color.WHITE)
        ));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoLetterLabel = new JLabel(" ");
        infoLetterLabel.setFont(new Font("Arial", Font.BOLD, 32));
        infoLetterLabel.setForeground(new Color(203, 32, 38));
        infoLetterLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        infoCard.add(infoLetterLabel);

        infoTitleLabel = new JLabel("Оберіть літеру...");
        infoTitleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        infoTitleLabel.setForeground(Color.WHITE);
        infoTitleLabel.setBorder(new EmptyBorder(0, 10, 5, 10));
        infoCard.add(infoTitleLabel);

        infoArea = new JTextArea("Натисніть на будь-яку літеру з абетки вище, щоб дізнатися її сакральний зміст та автоматично перенести орнамент-оберіг на полотно.");
        infoArea.setFont(new Font("Arial", Font.PLAIN, 11));
        infoArea.setForeground(new Color(180, 180, 180));
        infoArea.setBackground(new Color(35, 35, 35));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setBorder(new EmptyBorder(5, 10, 10, 10));
        infoCard.add(infoArea);

        JButton btnPlaceStamp = createStyledButton("Нанести орнамент літери", new Color(203, 32, 38));
        btnPlaceStamp.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPlaceStamp.addActionListener(e -> {
            if (activeStamp != null) {
                placeStampAtCenter();
            } else {
                JOptionPane.showMessageDialog(this, "Будь ласка, спочатку оберіть літеру з абетки!");
            }
        });
        infoCard.add(btnPlaceStamp);

        sidebar.add(infoCard);
        return sidebar;
    }

    private JScrollPane createCenterWorkspace() {
        canvasPanel = new EmbroideryCanvas();
        canvasPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(canvasPanel);
        scrollPane.setBackground(new Color(32, 32, 32));
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private JPanel createRightSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(new Color(26, 26, 26));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(40, 40, 40)));

        JLabel paletteTitle = new JLabel("Палітра кольорів");
        paletteTitle.setFont(new Font("Arial", Font.BOLD, 12));
        paletteTitle.setForeground(Color.LIGHT_GRAY);
        paletteTitle.setBorder(new EmptyBorder(15, 15, 5, 15));
        sidebar.add(paletteTitle);

        JPanel colorGrid = new JPanel(new GridLayout(3, 4, 6, 6));
        colorGrid.setBackground(new Color(26, 26, 26));
        colorGrid.setBorder(new EmptyBorder(5, 15, 15, 15));
        colorGrid.setMaximumSize(new Dimension(260, 120));

        for (Color col : traditionalColors) {
            JButton colBtn = new JButton();
            colBtn.setBackground(col);
            colBtn.setContentAreaFilled(false);
            colBtn.setOpaque(true);
            colBtn.setBorder(new LineBorder(new Color(60, 60, 60), 1));
            colBtn.addActionListener(e -> {
                activeColor = col;
                if (activeTool.equals("stamp")) {
                    activeTool = "brush";
                }
            });
            colorGrid.add(colBtn);
        }
        sidebar.add(colorGrid);

        JButton customColorBtn = createStyledButton("Обрати власний колір...", new Color(45, 45, 45));
        customColorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Оберіть колір нитки", activeColor);
            if (chosen != null) {
                activeColor = chosen;
                if (activeTool.equals("stamp")) activeTool = "brush";
            }
        });
        customColorBtn.setBorder(new EmptyBorder(5, 15, 15, 15));
        sidebar.add(customColorBtn);

        JLabel toolsTitle = new JLabel("Інструменти");
        toolsTitle.setFont(new Font("Arial", Font.BOLD, 12));
        toolsTitle.setForeground(Color.LIGHT_GRAY);
        toolsTitle.setBorder(new EmptyBorder(10, 15, 5, 15));
        sidebar.add(toolsTitle);

        JPanel toolsPanel = new JPanel(new GridLayout(4, 2, 6, 6));
        toolsPanel.setBackground(new Color(26, 26, 26));
        toolsPanel.setBorder(new EmptyBorder(5, 15, 15, 15));
        toolsPanel.setMaximumSize(new Dimension(260, 180));

        JButton btnBrush = createStyledButton("🖌️ Пензлик", new Color(203, 32, 38));
        btnBrush.addActionListener(e -> activeTool = "brush");
        toolsPanel.add(btnBrush);

        JButton btnEraser = createStyledButton("🧹 Ластик", new Color(50, 50, 50));
        btnEraser.addActionListener(e -> activeTool = "eraser");
        toolsPanel.add(btnEraser);

        JButton btnUndo = createStyledButton("↩️ Назад", new Color(50, 50, 50));
        btnUndo.addActionListener(e -> undo());
        toolsPanel.add(btnUndo);

        JButton btnClear = createStyledButton("🗑️ Стерти все", new Color(50, 50, 50));
        btnClear.addActionListener(e -> {
            saveState();
            initGrid();
            canvasPanel.repaint();
        });
        toolsPanel.add(btnClear);

        JButton btnMirrorH = createStyledButton("↔️ Віддзеркалити", new Color(50, 50, 50));
        btnMirrorH.addActionListener(e -> mirrorGridHorizontal());
        toolsPanel.add(btnMirrorH);

        JButton btnMirrorV = createStyledButton("↕️ Віддзеркалити", new Color(50, 50, 50));
        btnMirrorV.addActionListener(e -> mirrorGridVertical());
        toolsPanel.add(btnMirrorV);

        JButton btnDuplH = createStyledButton("➡️ Дубль вправо", new Color(50, 50, 50));
        btnDuplH.addActionListener(e -> duplicatePatternRight());
        toolsPanel.add(btnDuplH);

        JButton btnFill = createStyledButton("🪡 Залити все", new Color(50, 50, 50));
        btnFill.addActionListener(e -> fillCanvasWithColor());
        toolsPanel.add(btnFill);

        sidebar.add(toolsPanel);

        JLabel symTitle = new JLabel("Автосиметрія");
        symTitle.setFont(new Font("Arial", Font.BOLD, 12));
        symTitle.setForeground(Color.LIGHT_GRAY);
        symTitle.setBorder(new EmptyBorder(10, 15, 5, 15));
        sidebar.add(symTitle);

        JPanel symPanel = new JPanel();
        symPanel.setLayout(new BoxLayout(symPanel, BoxLayout.Y_AXIS));
        symPanel.setBackground(new Color(35, 35, 35));
        symPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        symPanel.setMaximumSize(new Dimension(230, 80));

        symVerticalBox = new JCheckBox("Вертикальна вісь");
        symVerticalBox.setForeground(Color.WHITE);
        symVerticalBox.setBackground(new Color(35, 35, 35));
        symPanel.add(symVerticalBox);

        symHorizontalBox = new JCheckBox("Горизонтальна вісь");
        symHorizontalBox.setForeground(Color.WHITE);
        symHorizontalBox.setBackground(new Color(35, 35, 35));
        symPanel.add(symHorizontalBox);

        sidebar.add(symPanel);

        JLabel displayTitle = new JLabel("Режим відображення");
        displayTitle.setFont(new Font("Arial", Font.BOLD, 12));
        displayTitle.setForeground(Color.LIGHT_GRAY);
        displayTitle.setBorder(new EmptyBorder(15, 15, 5, 15));
        sidebar.add(displayTitle);

        JPanel displayPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        displayPanel.setBackground(new Color(26, 26, 26));
        displayPanel.setBorder(new EmptyBorder(5, 15, 15, 15));
        displayPanel.setMaximumSize(new Dimension(260, 55));

        JButton btnModeCross = createStyledButton("Хрестик 🪡", new Color(203, 32, 38));
        btnModeCross.addActionListener(e -> {
            displayMode = "cross";
            canvasPanel.repaint();
        });
        displayPanel.add(btnModeCross);

        JButton btnModeFlat = createStyledButton("Пікселі ⬜", new Color(50, 50, 50));
        btnModeFlat.addActionListener(e -> {
            displayMode = "flat";
            canvasPanel.repaint();
        });
        displayPanel.add(btnModeFlat);

        sidebar.add(displayPanel);

        return sidebar;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bgColor.darker(), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectLetter(String letter) {
        LetterDatabase.LetterInfo info = LetterDatabase.alphabetData.get(letter);
        if (info != null) {
            infoLetterLabel.setText(info.name);
            infoTitleLabel.setText(info.title);
            infoArea.setText(info.desc);
            activeStamp = info.rune;
            activeTool = "stamp";
        }
    }

    private void saveState() {
        Color[][] clone = new Color[gridHeight][gridWidth];
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                clone[y][x] = gridData[y][x];
            }
        }
        historyStack.add(clone);
        if (historyStack.size() > MAX_HISTORY) {
            historyStack.remove(0);
        }
    }

    private void undo() {
        if (historyStack.size() > 1) {
            historyStack.remove(historyStack.size() - 1); // Удаляем текущее состояние
            Color[][] prevState = historyStack.get(historyStack.size() - 1);

            gridHeight = prevState.length;
            gridWidth = prevState[0].length;
            gridData = new Color[gridHeight][gridWidth];
            for (int y = 0; y < gridHeight; y++) {
                for (int x = 0; x < gridWidth; x++) {
                    gridData[y][x] = prevState[y][x];
                }
            }
            sizeDisplayLabel.setText("Сітка: " + gridWidth + " × " + gridHeight);
            canvasPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Немає кроків для відміни!");
        }
    }


    private void fillCanvasWithColor() {
        saveState();
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                gridData[y][x] = activeColor;
            }
        }
        canvasPanel.repaint();
    }

    private void mirrorGridHorizontal() {
        saveState();
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth / 2; x++) {
                Color temp = gridData[y][x];
                gridData[y][x] = gridData[y][gridWidth - 1 - x];
                gridData[y][gridWidth - 1 - x] = temp;
            }
        }
        canvasPanel.repaint();
    }

    private void mirrorGridVertical() {
        saveState();
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight / 2; y++) {
                Color temp = gridData[y][x];
                gridData[y][x] = gridData[gridHeight - 1 - y][x];
                gridData[gridHeight - 1 - y][x] = temp;
            }
        }
        canvasPanel.repaint();
    }

    private void duplicatePatternRight() {
        saveState();
        int minX = gridWidth, maxX = -1, minY = gridHeight, maxY = -1;
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                if (gridData[y][x] != null) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX == -1) {
            JOptionPane.showMessageDialog(this, "Полотно порожнє для дублювання!");
            return;
        }

        int patternWidth = maxX - minX + 1;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Color c = gridData[y][x];
                if (c != null) {
                    int tx = x + patternWidth;
                    if (tx < gridWidth) {
                        gridData[y][tx] = c;
                    }
                }
            }
        }
        canvasPanel.repaint();
    }

    private void placeStampAtCenter() {
        if (activeStamp == null) return;
        saveState();
        int sH = activeStamp.length;
        int sW = activeStamp[0].length;

        int startX = (gridWidth - sW) / 2;
        int startY = (gridHeight - sH) / 2;

        for (int y = 0; y < sH; y++) {
            for (int x = 0; x < sW; x++) {
                if (activeStamp[y][x] == 1) {
                    int tx = startX + x;
                    int ty = startY + y;
                    if (tx >= 0 && tx < gridWidth && ty >= 0 && ty < gridHeight) {
                        gridData[ty][tx] = activeColor;
                    }
                }
            }
        }
        canvasPanel.repaint();
    }

    private void changeSizeDialog() {
        JTextField widthField = new JTextField(String.valueOf(gridWidth), 5);
        JTextField heightField = new JTextField(String.valueOf(gridHeight), 5);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Ширина (10-120):"));
        myPanel.add(widthField);
        myPanel.add(Box.createHorizontalStrut(15)); // Разделитель
        myPanel.add(new JLabel("Висота (10-100):"));
        myPanel.add(heightField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Будь ласка, введіть розміри сітки", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int w = Integer.parseInt(widthField.getText());
                int h = Integer.parseInt(heightField.getText());
                if (w >= 10 && w <= 120 && h >= 10 && h <= 100) {
                    saveState();
                    gridWidth = w;
                    gridHeight = h;
                    initGrid();
                    sizeDisplayLabel.setText("Сітка: " + w + " × " + h);
                    canvasPanel.revalidate();
                    canvasPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Некоректний розмір! Обмеження: Ширина 10-120, Висота 10-100.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Будь ласка, введіть цілі числа!");
            }
        }
    }

    private void openTextToOrnamentDialog() {
        JDialog dialog = new JDialog(this, "Закодувати ім'я в узор", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(30, 30, 30));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel infoLbl = new JLabel("<html><b>Кодування імені у вишивку за традицією Бродівського письма</b><br>" +
                "Кожна літера слова перетвориться на унікальний оберіг на основі стародавньої геометричної символіки.</html>");
        infoLbl.setForeground(Color.LIGHT_GRAY);
        infoLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(infoLbl);
        mainPanel.add(Box.createVerticalStrut(15));

        JTextField inputWord = new JTextField("МАРІЯ");
        inputWord.setFont(new Font("Arial", Font.BOLD, 16));
        inputWord.setForeground(Color.WHITE);
        inputWord.setBackground(new Color(45, 45, 45));
        inputWord.setCaretColor(Color.WHITE);
        inputWord.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(inputWord);
        mainPanel.add(Box.createVerticalStrut(15));

        JRadioButton styleRune = new JRadioButton("Стародавні Руни (Текстовий геометричний шифр)", true);
        JRadioButton styleLetter = new JRadioButton("Вишитий шрифт (Візуальні літери)", false);
        ButtonGroup group = new ButtonGroup();
        group.add(styleRune);
        group.add(styleLetter);

        styleRune.setForeground(Color.WHITE);
        styleRune.setBackground(new Color(30, 30, 30));
        styleLetter.setForeground(Color.WHITE);
        styleLetter.setBackground(new Color(30, 30, 30));

        styleRune.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleLetter.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(styleRune);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(styleLetter);

        dialog.add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(25, 25, 25));

        JButton btnCancel = createStyledButton("Скасувати", new Color(50, 50, 50));
        btnCancel.addActionListener(e -> dialog.dispose());
        bottomPanel.add(btnCancel);

        JButton btnGenerate = createStyledButton("Згенерувати", new Color(203, 32, 38));
        btnGenerate.addActionListener(e -> {
            String word = inputWord.getText().trim().toUpperCase();
            if (word.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введіть слово!");
                return;
            }
            String chosenStyle = styleRune.isSelected() ? "rune" : "letter";
            generateWordOrnament(word, chosenStyle);
            dialog.dispose();
        });
        bottomPanel.add(btnGenerate);

        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void generateWordOrnament(String word, String style) {
        List<int[][]> matrices = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            LetterDatabase.LetterInfo info = LetterDatabase.alphabetData.get(String.valueOf(ch));
            if (info == null) {
                if (ch == 'I') info = LetterDatabase.alphabetData.get("І");
                else if (ch == 'E') info = LetterDatabase.alphabetData.get("Е");
                else if (ch == 'A') info = LetterDatabase.alphabetData.get("А");
                else if (ch == 'O') info = LetterDatabase.alphabetData.get("О");
                else if (ch == 'T') info = LetterDatabase.alphabetData.get("Т");
            }
            if (info != null) {
                matrices.add(style.equals("rune") ? info.rune : info.letter);
            }
        }

        if (matrices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Слово має містити українські літери!");
            return;
        }

        saveState();

        int letterWidth = 7;
        int spacing = 2;
        int totalWidth = (matrices.size() * letterWidth) + ((matrices.size() - 1) * spacing);


        if (totalWidth + 10 > gridWidth) {
            gridWidth = totalWidth + 12;
            initGrid();
            sizeDisplayLabel.setText("Сітка: " + gridWidth + " × " + gridHeight);
        }

        int startX = (gridWidth - totalWidth) / 2;
        int startY = (gridHeight - 7) / 2;


        for (int y = startY - 2; y < startY + 9; y++) {
            for (int x = startX - 2; x < startX + totalWidth + 2; x++) {
                if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                    gridData[y][x] = null;
                }
            }
        }


        int currentX = startX;
        for (int[][] matrix : matrices) {
            for (int y = 0; y < 7; y++) {
                for (int x = 0; x < 7; x++) {
                    if (matrix[y][x] == 1) {
                        int tx = currentX + x;
                        int ty = startY + y;
                        if (tx >= 0 && tx < gridWidth && ty >= 0 && ty < gridHeight) {
                            gridData[ty][tx] = activeColor;
                        }
                    }
                }
            }
            currentX += letterWidth + spacing;
        }

        canvasPanel.repaint();
    }


    private void saveAsPNG() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Зберегти проект вишиванки");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Изображения PNG (*.png)", "png"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }

            int exportCellSize = 16;
            BufferedImage img = new BufferedImage(gridWidth * exportCellSize, gridHeight * exportCellSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();


            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, img.getWidth(), img.getHeight());


            for (int y = 0; y < gridHeight; y++) {
                for (int x = 0; x < gridWidth; x++) {
                    Color c = gridData[y][x];
                    if (c != null) {
                        int px = x * exportCellSize;
                        int py = y * exportCellSize;

                        if (displayMode.equals("flat")) {
                            g2.setColor(c);
                            g2.fillRect(px, py, exportCellSize, exportCellSize);
                        } else {
                            g2.setColor(c);
                            g2.fillRect(px + 1, py + 1, exportCellSize - 2, exportCellSize - 2);


                            g2.setColor(new Color(0, 0, 0, 70));
                            g2.setStroke(new BasicStroke(1.5f));
                            g2.drawLine(px + 2, py + 2, px + exportCellSize - 2, py + exportCellSize - 2);


                            g2.setColor(new Color(255, 255, 255, 110));
                            g2.drawLine(px + exportCellSize - 2, py + 2, px + 2, py + exportCellSize - 2);
                        }
                    }
                }
            }

            g2.dispose();

            try {
                ImageIO.write(img, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Орнамент успішно збережено в PNG!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Помилка збереження файлу: " + ex.getMessage());
            }
        }
    }

    private class EmbroideryCanvas extends JPanel implements MouseListener, MouseMotionListener {

        public EmbroideryCanvas() {
            addMouseListener(this);
            addMouseMotionListener(this);
            setDoubleBuffered(true);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                    (int) (gridWidth * cellSize * zoomLevel),
                    (int) (gridHeight * cellSize * zoomLevel)
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;


            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());


            for (int y = 0; y < gridHeight; y++) {
                for (int x = 0; x < gridWidth; x++) {
                    Color c = gridData[y][x];
                    if (c != null) {
                        drawStitch(g2, x, y, c);
                    }
                }
            }


            if (showGrid) {
                g2.setColor(new Color(224, 224, 224));
                g2.setStroke(new BasicStroke(0.5f));

                int currentCellSize = (int)(cellSize * zoomLevel);


                for (int x = 0; x <= gridWidth; x++) {
                    int px = x * currentCellSize;
                    g2.drawLine(px, 0, px, getHeight());
                }


                for (int y = 0; y <= gridHeight; y++) {
                    int py = y * currentCellSize;
                    g2.drawLine(0, py, getWidth(), py);
                }
            }
        }

        private void drawStitch(Graphics2D g2, int x, int y, Color color) {
            int currentCellSize = (int)(cellSize * zoomLevel);
            int px = x * currentCellSize;
            int py = y * currentCellSize;

            if (displayMode.equals("flat")) {
                g2.setColor(color);
                g2.fillRect(px + 1, py + 1, currentCellSize - 1, currentCellSize - 1);
            } else {
                g2.setColor(color);

                g2.fillRect(px + 1, py + 1, currentCellSize - 2, currentCellSize - 2);

                g2.setStroke(new BasicStroke(1.5f));

                g2.setColor(new Color(0, 0, 0, 70));
                g2.drawLine(px + 2, py + 2, px + currentCellSize - 2, py + currentCellSize - 2);

                g2.setColor(new Color(255, 255, 255, 110));
                g2.drawLine(px + currentCellSize - 2, py + 2, px + 2, py + currentCellSize - 2);
            }
        }


        private void processMouseDraw(MouseEvent e) {
            int currentCellSize = (int)(cellSize * zoomLevel);
            int x = e.getX() / currentCellSize;
            int y = e.getY() / currentCellSize;

            if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {

                boolean erase = SwingUtilities.isRightMouseButton(e) || activeTool.equals("eraser");

                if (activeTool.equals("stamp") && activeStamp != null && !erase) {
                    applyStampAt(x, y);
                } else {
                    paintPixel(x, y, erase);
                }
            }
        }


        private void paintPixel(int x, int y, boolean erase) {
            boolean symV = symVerticalBox.isSelected();
            boolean symH = symHorizontalBox.isSelected();

            java.util.List<Point> points = new ArrayList<>();
            points.add(new Point(x, y));

            if (symV) {
                points.add(new Point(gridWidth - 1 - x, y));
            }
            if (symH) {
                points.add(new Point(x, gridHeight - 1 - y));
            }
            if (symV && symH) {
                points.add(new Point(gridWidth - 1 - x, gridHeight - 1 - y));
            }

            boolean stateChanged = false;
            for (Point p : points) {
                if (p.x >= 0 && p.x < gridWidth && p.y >= 0 && p.y < gridHeight) {
                    if (erase) {
                        if (gridData[p.y][p.x] != null) {
                            gridData[p.y][p.x] = null;
                            stateChanged = true;
                        }
                    } else {
                        if (gridData[p.y][p.x] == null || !gridData[p.y][p.x].equals(activeColor)) {
                            gridData[p.y][p.x] = activeColor;
                            stateChanged = true;
                        }
                    }
                }
            }

            if (stateChanged) {
                repaint();
            }
        }

        private void applyStampAt(int centerX, int centerY) {
            if (activeStamp == null) return;
            int sH = activeStamp.length;
            int sW = activeStamp[0].length;
            int startX = centerX - sW / 2;
            int startY = centerY - sH / 2;

            for (int y = 0; y < sH; y++) {
                for (int x = 0; x < sW; x++) {
                    if (activeStamp[y][x] == 1) {
                        int tx = startX + x;
                        int ty = startY + y;
                        if (tx >= 0 && tx < gridWidth && ty >= 0 && ty < gridHeight) {
                            gridData[ty][tx] = activeColor;
                        }
                    }
                }
            }
            repaint();
        }

        @Override public void mousePressed(MouseEvent e) { saveState(); processMouseDraw(e); }
        @Override public void mouseDragged(MouseEvent e) { processMouseDraw(e); }
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        @Override public void mouseMoved(MouseEvent e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VyshyvankaEditor frame = new VyshyvankaEditor();
            frame.setVisible(true);
        });
    }
}


class LetterDatabase {

    public static class LetterInfo {
        public String name;
        public String title;
        public String desc;
        public int[][] rune;
        public int[][] letter;

        public LetterInfo(String name, String title, String desc, int[][] rune, int[][] letter) {
            this.name = name;
            this.title = title;
            this.desc = desc;
            this.rune = rune;
            this.letter = letter;
        }
    }

    public static final Map<String, LetterInfo> alphabetData = new HashMap<>();

    static {
        // Литера А
        alphabetData.put("А", new LetterInfo("А", "Початок, Сонце, Першооснова",
                "Символізує зародження нового життя, джерело космічної енергії та чисте світло. Закладає фундамент життєвого шляху.",
                new int[][]{
                        {0,0,1,1,1,0,0},
                        {0,1,0,0,0,1,0},
                        {1,0,0,1,0,0,1},
                        {1,0,1,1,1,0,1},
                        {1,0,0,1,0,0,1},
                        {0,1,0,0,0,1,0},
                        {0,0,1,1,1,0,0}
                },
                new int[][]{
                        {0,0,1,1,1,0,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,1,1,1,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0}
                }
        ));

        // Литера Б
        alphabetData.put("Б", new LetterInfo("Б", "Божественна сила, Оберіг",
                "Відображає вищу божественну охорону, захищає від злих сил та лихого ока. Надійний оберіг для дітей та родини.",
                new int[][]{
                        {1,1,1,1,1,1,1},
                        {1,0,0,0,0,0,0},
                        {1,0,1,1,1,1,0},
                        {1,0,1,0,0,1,0},
                        {1,0,1,1,1,1,0},
                        {1,0,0,0,0,0,1},
                        {1,1,1,1,1,1,0}
                },
                new int[][]{
                        {0,1,1,1,1,1,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,1,1,1,0,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,1,1,1,0,0}
                }
        ));

        // Литера В
        alphabetData.put("В", new LetterInfo("В", "Вода, Життя, Мудрість",
                "Символ життєдайної вологи, плину часу та досвіду пращурів. Несе гармонію у вирішенні життєвих негараздів.",
                new int[][]{
                        {0,1,1,0,1,1,0},
                        {1,0,0,1,0,0,1},
                        {1,0,0,0,0,0,1},
                        {0,1,0,0,0,1,0},
                        {1,0,0,0,0,0,1},
                        {1,0,0,1,0,0,1},
                        {0,1,1,0,1,1,0}
                },
                new int[][]{
                        {0,1,1,1,1,0,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,1,1,1,0,0},
                        {0,1,0,0,0,1,0},
                        {0,0,1,0,0,1,0},
                        {0,1,1,1,1,0,0}
                }
        ));

        // Литера Г
        alphabetData.put("Г", new LetterInfo("Г", "Земля, Сварга, Господарство",
                "Уособлює родючість землі-матінки, стабільність, міцний зв'язок з домом та успіх у господарських і фінансових справах.",
                new int[][]{
                        {1,1,1,1,1,1,1},
                        {1,0,0,0,0,0,0},
                        {1,0,0,0,0,0,0},
                        {1,0,1,1,1,1,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,1,1,1,1,1,1}
                },
                new int[][]{
                        {0,1,1,1,1,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0}
                }
        ));

        // Литера Д
        alphabetData.put("Д", new LetterInfo("Д", "Дім, Добро, Спадщина",
                "Символ надійного вогнища, сімейного затишку та міцної брами. Охороняє цілісність житла від злих намірів.",
                new int[][]{
                        {0,0,0,1,0,0,0},
                        {0,0,1,1,1,0,0},
                        {0,1,0,1,0,1,0},
                        {1,1,1,1,1,1,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,1,1,1,1,1,1}
                },
                new int[][]{
                        {0,0,0,1,1,0,0},
                        {0,0,1,0,1,0,0},
                        {0,1,0,0,1,0,0},
                        {0,1,1,1,1,1,0},
                        {1,1,0,0,1,1,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1}
                }
        ));

        // Литера Е
        alphabetData.put("Е", new LetterInfo("Е", "Енергія Всесвіту, Простір",
                "Асоціюється з легкістю, життєвою силою, внутрішньою гармонією та відкритістю новим горизонтам.",
                new int[][]{
                        {1,1,1,1,1,1,1},
                        {1,0,0,0,0,0,0},
                        {1,0,1,1,1,1,1},
                        {1,0,0,0,0,0,1},
                        {1,0,1,1,1,1,1},
                        {1,0,0,0,0,0,0},
                        {1,1,1,1,1,1,1}
                },
                new int[][]{
                        {0,1,1,1,1,1,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,1,1,1,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,1,1,1,1,0}
                }
        ));

        // Литера Ж
        alphabetData.put("Ж", new LetterInfo("Ж", "Життя, Берегиня, Світ",
                "Один із головних жіночих оберегів. Формує сильний захисний купол здоров'я та квітучого довголіття.",
                new int[][]{
                        {1,0,1,0,1,0,1},
                        {0,1,0,1,0,1,0},
                        {1,0,1,0,1,0,1},
                        {0,0,0,1,0,0,0},
                        {1,0,1,0,1,0,1},
                        {0,1,0,1,0,1,0},
                        {1,0,1,0,1,0,1}
                },
                new int[][]{
                        {1,0,0,1,0,0,1},
                        {1,0,1,0,1,0,1},
                        {0,1,0,1,0,1,0},
                        {0,0,1,1,1,0,0},
                        {0,1,0,1,0,1,0},
                        {1,0,1,0,1,0,1},
                        {1,0,0,1,0,0,1}
                }
        ));

        // Литера З
        alphabetData.put("З", new LetterInfo("З", "Здоров'я, Земна Сила",
                "Дарує витривалість, фізичне оновлення, зцілення душі та силу вистояти у будь-яких випробуваннях.",
                new int[][]{
                        {1,1,1,1,1,1,1},
                        {0,0,0,0,0,0,1},
                        {0,1,1,1,1,1,0},
                        {0,0,0,0,0,0,1},
                        {0,1,1,1,1,1,0},
                        {0,0,0,0,0,0,1},
                        {1,1,1,1,1,1,1}
                },
                new int[][]{
                        {0,1,1,1,1,0,0},
                        {1,0,0,0,0,1,0},
                        {0,0,0,0,1,0,0},
                        {0,0,1,1,1,0,0},
                        {0,0,0,0,0,1,0},
                        {1,0,0,0,0,1,0},
                        {0,1,1,1,1,0,0}
                }
        ));

        // Литера І
        alphabetData.put("І", new LetterInfo("І", "Інтуїція, Боже світло",
                "Дарує гостроту розуму, глибинне внутрішнє бачення та розвиває інтуїтивні здібності.",
                new int[][]{
                        {0,0,1,1,1,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,0,0,0,0},
                        {0,0,1,1,1,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,1,1,1,0,0}
                },
                new int[][]{
                        {0,0,0,1,0,0,0},
                        {0,0,0,0,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0}
                }
        ));

        // Литера К
        alphabetData.put("К", new LetterInfo("К", "Космос, Порядок",
                "Встановлює рівновагу у просторі, структурує хаос і вносить гармонію у взаємини.",
                new int[][]{
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,1,0},
                        {1,0,0,0,1,0,0},
                        {1,1,1,1,0,0,0},
                        {1,0,0,0,1,0,0},
                        {1,0,0,0,0,1,0},
                        {1,0,0,0,0,0,1}
                },
                new int[][]{
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,1,0,0},
                        {0,1,0,1,0,0,0},
                        {0,1,1,0,0,0,0},
                        {0,1,0,1,0,0,0},
                        {0,1,0,0,1,0,0},
                        {0,1,0,0,0,1,0}
                }
        ));

        // Литера Л
        alphabetData.put("Л", new LetterInfo("Л", "Любов, Світло, Мир",
                "Несе м'яку енергію вселенської любові, робить людину привабливою, дарує спокій та умиротворення.",
                new int[][]{
                        {0,0,0,1,0,0,0},
                        {0,0,1,1,1,0,0},
                        {0,1,0,1,0,1,0},
                        {1,0,0,1,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1}
                },
                new int[][]{
                        {0,0,0,1,1,0,0},
                        {0,0,1,0,1,0,0},
                        {0,1,0,0,1,0,0},
                        {0,1,0,0,1,0,0},
                        {0,1,0,0,1,0,0},
                        {0,1,0,0,1,0,0},
                        {1,1,0,0,1,1,1}
                }
        ));

        // Литера М
        alphabetData.put("М", new LetterInfo("М", "Мати, Мислення",
                "Символ Берегині роду, материнської любові та чистих помислів. Підходить для захисту сім'ї.",
                new int[][]{
                        {1,0,0,0,0,0,1},
                        {1,1,0,0,0,1,1},
                        {1,0,1,0,1,0,1},
                        {1,0,0,1,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1}
                },
                new int[][]{
                        {0,1,0,0,0,1,0},
                        {0,1,1,0,1,1,0},
                        {0,1,0,1,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0}
                }
        ));

        // Литера Н
        alphabetData.put("Н", new LetterInfo("Н", "Надійність, Небо",
                "Оберіг, що вирівнює баланс між духовним та земним. Дарує надійних друзів та стійкість.",
                new int[][]{
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,1,1,1,1,1,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1}
                },
                new int[][]{
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,1,1,1,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0}
                }
        ));

        // Литера О
        alphabetData.put("О", new LetterInfo("О", "Об'єднання, Колообіг",
                "Символізує цілісність, завершеність життєвого циклу. Потужний щит від зовнішнього негативу.",
                new int[][]{
                        {0,1,1,1,1,1,0},
                        {1,0,0,0,0,0,1},
                        {1,0,0,1,0,0,1},
                        {1,0,1,1,1,0,1},
                        {1,0,0,1,0,0,1},
                        {1,0,0,0,0,0,1},
                        {0,1,1,1,1,1,0}
                },
                new int[][]{
                        {0,0,1,1,1,0,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,0,1,1,1,0,0}
                }
        ));

        // Литера Р
        alphabetData.put("Р", new LetterInfo("Р", "Радість, Родина",
                "Створює сприятливу ауру для зміцнення сімейних зв'язків, несе світло і радісні зміни.",
                new int[][]{
                        {1,1,1,1,1,1,0},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {1,1,1,1,1,1,0},
                        {1,0,0,0,0,0,0},
                        {1,0,0,0,0,0,0},
                        {1,0,0,0,0,0,0}
                },
                new int[][]{
                        {0,1,1,1,1,0,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,1,1,1,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0}
                }
        ));

        // Литера С
        alphabetData.put("С", new LetterInfo("С", "Слово, Світло, Сила",
                "Оберігає чистоту думок, наділяє красномовством і допомагає порозумітися з оточуючими.",
                new int[][]{
                        {0,1,1,1,1,1,1},
                        {1,0,0,0,0,0,0},
                        {1,0,0,0,0,0,0},
                        {1,0,0,0,0,0,0},
                        {1,0,0,0,0,0,0},
                        {1,0,0,0,0,0,0},
                        {0,1,1,1,1,1,1}
                },
                new int[][]{
                        {0,0,1,1,1,1,0},
                        {0,1,0,0,0,0,1},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,0},
                        {0,1,0,0,0,0,1},
                        {0,0,1,1,1,1,0}
                }
        ));

        // Литера Т
        alphabetData.put("Т", new LetterInfo("Т", "Твердість, Терпіння",
                "Наділяє непохитністю у переконаннях, витримкою та силою для завершення складних справ.",
                new int[][]{
                        {1,1,1,1,1,1,1},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0}
                },
                new int[][]{
                        {0,1,1,1,1,1,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0},
                        {0,0,0,1,0,0,0}
                }
        ));

        // Литера Я
        alphabetData.put("Я", new LetterInfo("Я", "Ясність, Самодостатність",
                "Символізує гармонійну самоідентифікацію, впевненість у власних силах, лідерство та мудрість.",
                new int[][]{
                        {0,1,1,1,1,1,1},
                        {1,0,0,0,0,0,1},
                        {1,0,0,0,0,0,1},
                        {0,1,1,1,1,1,1},
                        {0,0,0,0,0,1,0},
                        {0,0,0,0,1,0,0},
                        {0,0,0,1,0,0,0}
                },
                new int[][]{
                        {0,0,1,1,1,1,0},
                        {0,1,0,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {0,0,1,1,1,1,0},
                        {0,0,1,0,0,1,0},
                        {0,1,0,0,0,1,0},
                        {1,1,0,0,0,1,1}
                }
        ));
    }
}