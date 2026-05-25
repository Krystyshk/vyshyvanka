import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Stack;

public class EmbroideryFrame extends JFrame {
    private static final int MIN_CELL_SIZE = 6;
    private static final int MAX_CELL_SIZE = 36;

    private final Color brown = new Color(125, 55, 50);
    private final Color borderBlue = new Color(175, 205, 230);

    private int rows = 50;
    private int cols = 52;
    private int cellSize = 15;

    private Color[][] cells;
    private Color selectedColor = new Color(190, 0, 0);

    private CanvasPanel canvasPanel;
    private JButton gridButton;
    private JLabel statusLabel;

    private boolean eraserMode = false;

    private boolean horizontalSymmetry = false;
    private boolean verticalSymmetry = false;
    private boolean hideGrid = false;

    private boolean cloneMode = false;
    private boolean cloningNow = false;
    private boolean selectingCloneZone = false;
    private boolean cloneZoneReady = false;

    private boolean moveMode = false;
    private boolean movingNow = false;
    private boolean selectingMoveZone = false;
    private boolean moveZoneReady = false;

    private int selectStartRow;
    private int selectStartCol;
    private int selectEndRow;
    private int selectEndCol;

    private int moveStartRow;
    private int moveStartCol;
    private int moveEndRow;
    private int moveEndCol;

    private Color[][] clonePattern;
    private int cloneRows;
    private int cloneCols;
    private int cloneOffsetRow;
    private int cloneOffsetCol;
    private int cloneTargetRow;
    private int cloneTargetCol;

    private Color[][] movePattern;
    private int moveRows;
    private int moveCols;
    private int moveTargetRow;
    private int moveTargetCol;
    private int moveOriginalRow;
    private int moveOriginalCol;

    private JCheckBox horizontalCheckBox;
    private JCheckBox verticalCheckBox;
    private JCheckBox noSymmetryCheckBox;
    private JCheckBox hideGridCheckBox;
    private JCheckBox cloneCheckBox;
    private JCheckBox moveCheckBox;

    private final Stack<Color[][]> undoStack = new Stack<>();
    private final java.util.List<SavedDesign> savedDesigns = new java.util.ArrayList<>();
    private final File historyFile = new File("constructor_history.dat");

    public EmbroideryFrame() {
        super("Редактор схем вишивки — Попкова Кристина");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        AppWindow.setup(this);

        createCells();
        loadHistoryFromFile();

        setLayout(new BorderLayout());

        createMenuBar();
        createToolbar();
        createMainContent();
        createStatusBar();

        drawDefaultNameOnStart();
    }

    private void drawDefaultNameOnStart() {
        drawNameAsOneCombinedOrnament("КРИСТИНА");

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void createCells() {
        cells = new Color[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = Color.WHITE;
            }
        }
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("Файл");

        JMenuItem clear = new JMenuItem("Очистити");
        clear.addActionListener(e -> clearCanvas());

        JMenuItem exit = new JMenuItem("Вийти");
        exit.addActionListener(e -> dispose());

        file.add(clear);
        file.add(exit);

        JMenuItem help = new JMenuItem("Довідка");
        help.addActionListener(e -> showHelpDialog());

        menuBar.add(file);
        menuBar.add(help);

        setJMenuBar(menuBar);
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

                МОЖЛИВОСТІ ПРОГРАМИ:

                • Малювання орнаментів на сітці
                • Вибір кольору з палітри
                • Використання пензлика та ластика
                • Очищення полотна
                • Створення орнаменту з тексту
                • Збереження орнаменту у PNG
                • Відкриття PNG
                • Збереження роботи в історію
                • Перегляд історії
                • Збільшення та зменшення полотна

                ІНСТРУМЕНТИ:

                Пензлик — малює вибраним кольором.
                Ластик — очищає клітинки.
                Стерти все — очищає все полотно.
                Дубль → — дублює орнамент вправо.
                Дубль ↓ — дублює орнамент вниз.
                Назад — повертає попередню дію.

                АВТОМАТИЗАЦІЯ:

                Горизонтальна вісь — симетрія по горизонталі.
                Вертикальна вісь — симетрія по вертикалі.
                Без симетрії — звичайне малювання.
                Прибрати сітку — приховує лінії сітки.
                Клонування — повторення вибраного фрагмента.
                Переміщення — рух вибраного фрагмента.
                """);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 450));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Довідка",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 7));
        toolbar.setBackground(Color.WHITE);
        toolbar.setPreferredSize(new Dimension(AppWindow.WINDOW_W, 48));
        toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, borderBlue));

        gridButton = toolbarButton("Сітка: " + rows + " x " + cols);
        gridButton.addActionListener(e -> changeGridSize());

        JButton zoomOutButton = toolbarSmallButton("−");
        zoomOutButton.setToolTipText("Віддалити полотно");
        zoomOutButton.addActionListener(e -> changeZoom(-2));

        JButton zoomInButton = toolbarSmallButton("+");
        zoomInButton.setToolTipText("Приблизити полотно");
        zoomInButton.addActionListener(e -> changeZoom(2));

        JButton textButton = toolbarButton("Текст → орнамент");
        textButton.addActionListener(e -> generateTextPattern());

        JButton saveButton = toolbarButton("Зберегти PNG");
        saveButton.addActionListener(e -> savePng());

        JButton openButton = toolbarButton("Відкрити PNG");
        openButton.addActionListener(e -> openPng());

        JButton saveHistoryButton = toolbarButton("Зберегти в історію");
        saveHistoryButton.addActionListener(e -> saveToConstructorHistory());

        JButton openHistoryButton = toolbarButton("Історія");
        openHistoryButton.addActionListener(e -> openConstructorHistory());

        JButton helpButton = toolbarButton("Довідка");
        helpButton.addActionListener(e -> showHelpDialog());

        JButton menuButton = toolbarButton("До меню");
        menuButton.addActionListener(e -> AppWindow.open(this, new HomeFrame()));

        toolbar.add(gridButton);
        toolbar.add(zoomOutButton);
        toolbar.add(zoomInButton);
        toolbar.add(textButton);
        toolbar.add(saveButton);
        toolbar.add(openButton);
        toolbar.add(saveHistoryButton);
        toolbar.add(openHistoryButton);
        toolbar.add(helpButton);
        toolbar.add(menuButton);

        add(toolbar, BorderLayout.NORTH);
    }

    private JButton toolbarButton(String text) {
        JButton button = new JButton(text);

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
        button.setPreferredSize(new Dimension(145, 34));

        return button;
    }

    private JButton toolbarSmallButton(String text) {
        JButton button = toolbarButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(42, 34));
        button.setMinimumSize(new Dimension(42, 34));
        button.setMaximumSize(new Dimension(42, 34));
        return button;
    }

    private void changeZoom(int step) {
        int oldCellSize = cellSize;
        cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, cellSize + step));

        if (cellSize == oldCellSize) {
            return;
        }

        updateCanvasSize();

        if (statusLabel != null) {
            int percent = Math.round(cellSize * 100f / 15f);
            statusLabel.setText("Масштаб полотна: " + percent + "%");
        }
    }

    private void updateCanvasSize() {
        if (canvasPanel == null) {
            return;
        }

        canvasPanel.setPreferredSize(new Dimension(cols * cellSize + 1, rows * cellSize + 1));
        canvasPanel.setSize(new Dimension(cols * cellSize + 1, rows * cellSize + 1));
        canvasPanel.revalidate();
        canvasPanel.repaint();
    }

    private void createMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        canvasPanel = new CanvasPanel();
        canvasPanel.setPreferredSize(new Dimension(cols * cellSize + 1, rows * cellSize + 1));

        JScrollPane canvasScroll = new JScrollPane(canvasPanel);
        canvasScroll.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
        canvasScroll.setBackground(Color.WHITE);
        canvasScroll.getViewport().setBackground(Color.WHITE);
        canvasScroll.getHorizontalScrollBar().setUnitIncrement(18);
        canvasScroll.getVerticalScrollBar().setUnitIncrement(18);
        canvasScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        canvasScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        centerWrapper.add(canvasScroll, BorderLayout.CENTER);

        main.add(leftPanel, BorderLayout.WEST);
        main.add(centerWrapper, BorderLayout.CENTER);
        main.add(rightPanel, BorderLayout.EAST);

        add(main, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(310, 1));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 2, borderBlue),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JLabel title = new JLabel("Абетка та орнаменти", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 19));
        title.setForeground(brown);
        title.setBounds(15, 15, 280, 30);
        panel.add(title);

        JPanel content = new JPanel(null);
        content.setBackground(Color.WHITE);
        content.setBounds(22, 58, 266, 575);
        content.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
        panel.add(content);

        JLabel ornamentTitle = leftSectionTitle("Орнаменти");
        ornamentTitle.setBounds(0, 12, 266, 24);
        content.add(ornamentTitle);

        JPanel ornamentGrid = new JPanel(new GridLayout(2, 3, 8, 8));
        ornamentGrid.setBackground(Color.WHITE);
        ornamentGrid.setBounds(25, 45, 216, 88);

        ornamentGrid.add(leftGridButton("Піксель", () -> drawSimplePattern("Піксель")));
        ornamentGrid.add(leftGridButton("Ромб", () -> drawSimplePattern("Ромб")));
        ornamentGrid.add(leftGridButton("Хрест", () -> drawSimplePattern("Хрест")));
        ornamentGrid.add(leftGridButton("Дерево", () -> drawSimplePattern("Дерево життя")));
        ornamentGrid.add(leftGridButton("Зірка", () -> drawSimplePattern("Зірка")));
        ornamentGrid.add(leftGridButton("Калина", () -> drawSimplePattern("Калина")));

        content.add(ornamentGrid);

        JLabel lettersTitle = leftSectionTitle("Українська абетка");
        lettersTitle.setBounds(0, 155, 266, 24);
        content.add(lettersTitle);

        JPanel lettersGrid = new JPanel(new GridLayout(8, 4, 8, 8));
        lettersGrid.setBackground(Color.WHITE);
        lettersGrid.setBounds(31, 190, 204, 344);

        String letters = "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ";

        for (int i = 0; i < letters.length(); i++) {
            String letter = String.valueOf(letters.charAt(i));
            lettersGrid.add(leftLetterButton(letter));
        }

        content.add(lettersGrid);

        return panel;
    }

    private JLabel leftSectionTitle(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(Color.BLACK);
        return label;
    }

    private JButton leftGridButton(String text, Runnable action) {
        JButton button = new JButton("<html><div style='text-align:center;'>" + text + "</div></html>");

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
        button.setPreferredSize(new Dimension(66, 38));

        button.addActionListener(e -> action.run());

        return button;
    }

    private JButton leftLetterButton(String letter) {
        JButton button = new JButton(letter);

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Georgia", Font.BOLD, 17));
        button.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
        button.setPreferredSize(new Dimension(45, 35));

        button.addActionListener(e -> drawLetter(letter));

        return button;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(285, 1));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, borderBlue));

        JLabel paletteTitle = rightTitle("Палітра кольорів");
        paletteTitle.setBounds(15, 15, 240, 24);
        panel.add(paletteTitle);

        JPanel palettePanel = new JPanel(new GridLayout(8, 8, 3, 3));
        palettePanel.setBackground(Color.WHITE);
        palettePanel.setBounds(15, 42, 245, 205);

        Color[] colors = createPaletteColors();

        for (Color color : colors) {
            JButton colorButton = new JButton();
            colorButton.setBackground(color);
            colorButton.setFocusPainted(false);
            colorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            colorButton.setBorder(BorderFactory.createLineBorder(borderBlue, 1));
            colorButton.setOpaque(true);

            colorButton.addActionListener(e -> {
                selectedColor = color;
                eraserMode = false;
                cloneMode = false;
                moveMode = false;

                if (cloneCheckBox != null) {
                    cloneCheckBox.setSelected(false);
                }

                if (moveCheckBox != null) {
                    moveCheckBox.setSelected(false);
                }

                if (statusLabel != null) {
                    statusLabel.setText("Обрано колір");
                }
            });

            palettePanel.add(colorButton);
        }

        panel.add(palettePanel);

        JButton chooseColor = rightButton("Обрати колір...");
        chooseColor.setBounds(15, 254, 245, 32);
        chooseColor.addActionListener(e -> openColorChooser());
        panel.add(chooseColor);

        JLabel toolsTitle = rightTitle("Інструменти");
        toolsTitle.setBounds(15, 295, 240, 24);
        panel.add(toolsTitle);

        JPanel toolsPanel = new JPanel(new GridLayout(3, 2, 7, 7));
        toolsPanel.setBackground(Color.WHITE);
        toolsPanel.setBounds(15, 323, 245, 125);

        JButton brush = rightButton("Пензлик");
        brush.addActionListener(e -> {
            cloneMode = false;
            moveMode = false;
            eraserMode = false;

            if (cloneCheckBox != null) {
                cloneCheckBox.setSelected(false);
            }

            if (moveCheckBox != null) {
                moveCheckBox.setSelected(false);
            }

            if (statusLabel != null) {
                statusLabel.setText("Інструмент: пензлик");
            }
        });

        JButton eraser = rightButton("Ластик");
        eraser.addActionListener(e -> {
            cloneMode = false;
            moveMode = false;
            eraserMode = true;

            if (cloneCheckBox != null) {
                cloneCheckBox.setSelected(false);
            }

            if (moveCheckBox != null) {
                moveCheckBox.setSelected(false);
            }

            if (statusLabel != null) {
                statusLabel.setText("Інструмент: ластик");
            }
        });

        JButton clear = rightButton("Стерти все");
        clear.addActionListener(e -> clearCanvas());

        JButton duplicateRight = rightButton("Дубль →");
        duplicateRight.addActionListener(e -> duplicateRight());

        JButton duplicateDown = rightButton("Дубль ↓");
        duplicateDown.addActionListener(e -> duplicateDown());

        JButton undo = rightButton("Назад");
        undo.addActionListener(e -> undoAction());

        toolsPanel.add(brush);
        toolsPanel.add(eraser);
        toolsPanel.add(clear);
        toolsPanel.add(duplicateRight);
        toolsPanel.add(duplicateDown);
        toolsPanel.add(undo);

        panel.add(toolsPanel);

        JLabel autoTitle = rightTitle("Автоматизація");
        autoTitle.setBounds(15, 460, 240, 24);
        panel.add(autoTitle);

        JPanel autoPanel = new JPanel(new GridLayout(6, 1, 0, 0));
        autoPanel.setBackground(Color.WHITE);
        autoPanel.setBounds(15, 485, 245, 165);

        horizontalCheckBox = automationCheckBox("Горизонтальна вісь");
        verticalCheckBox = automationCheckBox("Вертикальна вісь");
        noSymmetryCheckBox = automationCheckBox("Без симетрії");
        hideGridCheckBox = automationCheckBox("Прибрати сітку");
        cloneCheckBox = automationCheckBox("Клонування");
        moveCheckBox = automationCheckBox("Переміщення");

        noSymmetryCheckBox.setSelected(true);

        horizontalCheckBox.addActionListener(e -> {
            horizontalSymmetry = horizontalCheckBox.isSelected();

            if (horizontalSymmetry || verticalSymmetry) {
                noSymmetryCheckBox.setSelected(false);
            }

            if (!horizontalSymmetry && !verticalSymmetry) {
                noSymmetryCheckBox.setSelected(true);
            }

            updateSymmetryStatus();
        });

        verticalCheckBox.addActionListener(e -> {
            verticalSymmetry = verticalCheckBox.isSelected();

            if (horizontalSymmetry || verticalSymmetry) {
                noSymmetryCheckBox.setSelected(false);
            }

            if (!horizontalSymmetry && !verticalSymmetry) {
                noSymmetryCheckBox.setSelected(true);
            }

            updateSymmetryStatus();
        });

        noSymmetryCheckBox.addActionListener(e -> {
            if (noSymmetryCheckBox.isSelected()) {
                horizontalSymmetry = false;
                verticalSymmetry = false;

                horizontalCheckBox.setSelected(false);
                verticalCheckBox.setSelected(false);

                if (statusLabel != null) {
                    statusLabel.setText("Симетрію вимкнено");
                }
            }
        });

        hideGridCheckBox.addActionListener(e -> {
            hideGrid = hideGridCheckBox.isSelected();

            if (canvasPanel != null) {
                canvasPanel.repaint();
            }

            if (statusLabel != null) {
                if (hideGrid) {
                    statusLabel.setText("Сітку приховано. PNG буде без сітки");
                } else {
                    statusLabel.setText("Сітку показано. PNG буде з сіткою");
                }
            }
        });

        cloneCheckBox.addActionListener(e -> {
            cloneMode = cloneCheckBox.isSelected();

            cloningNow = false;
            selectingCloneZone = false;
            cloneZoneReady = false;
            clonePattern = null;

            if (cloneMode) {
                moveMode = false;
                movingNow = false;
                selectingMoveZone = false;
                moveZoneReady = false;
                movePattern = null;
                eraserMode = false;

                if (moveCheckBox != null) {
                    moveCheckBox.setSelected(false);
                }

                if (statusLabel != null) {
                    statusLabel.setText("Клонування: виділи зону, яку хочеш клонувати");
                }
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("Клонування вимкнено");
                }
            }

            if (canvasPanel != null) {
                canvasPanel.repaint();
            }
        });

        moveCheckBox.addActionListener(e -> {
            moveMode = moveCheckBox.isSelected();

            movingNow = false;
            selectingMoveZone = false;
            moveZoneReady = false;
            movePattern = null;

            if (moveMode) {
                cloneMode = false;
                cloningNow = false;
                selectingCloneZone = false;
                cloneZoneReady = false;
                clonePattern = null;
                eraserMode = false;

                if (cloneCheckBox != null) {
                    cloneCheckBox.setSelected(false);
                }

                if (statusLabel != null) {
                    statusLabel.setText("Переміщення: виділи деталь, яку хочеш перенести");
                }
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("Переміщення вимкнено");
                }
            }

            if (canvasPanel != null) {
                canvasPanel.repaint();
            }
        });

        autoPanel.add(horizontalCheckBox);
        autoPanel.add(verticalCheckBox);
        autoPanel.add(noSymmetryCheckBox);
        autoPanel.add(hideGridCheckBox);
        autoPanel.add(cloneCheckBox);
        autoPanel.add(moveCheckBox);

        panel.add(autoPanel);

        return panel;
    }

    private Color[] createPaletteColors() {
        return new Color[]{
                new Color(190, 0, 0), new Color(220, 0, 0), new Color(245, 40, 40), new Color(255, 85, 85),
                new Color(255, 130, 130), new Color(255, 180, 180), new Color(255, 220, 220), new Color(255, 240, 240),
                new Color(140, 70, 0), new Color(180, 95, 0), new Color(220, 130, 0), new Color(245, 170, 20),
                new Color(255, 210, 40), new Color(255, 240, 80), new Color(210, 235, 70), new Color(165, 220, 70),
                new Color(20, 90, 75), new Color(25, 120, 80), new Color(35, 150, 95), new Color(55, 180, 110),
                new Color(90, 205, 135), new Color(135, 225, 165), new Color(175, 240, 200), new Color(215, 250, 225),
                new Color(10, 70, 130), new Color(0, 95, 160), new Color(0, 125, 185), new Color(30, 155, 205),
                new Color(75, 185, 225), new Color(120, 210, 240), new Color(170, 230, 250), new Color(220, 245, 255),
                new Color(45, 40, 145), new Color(75, 55, 190), new Color(100, 70, 220), new Color(130, 90, 235),
                new Color(165, 115, 240), new Color(200, 150, 245), new Color(225, 185, 250), new Color(245, 220, 255),
                new Color(120, 0, 80), new Color(155, 0, 120), new Color(190, 0, 165), new Color(220, 40, 190),
                new Color(240, 90, 215), new Color(250, 140, 230), new Color(255, 190, 240), new Color(255, 225, 250),
                Color.BLACK, new Color(35, 35, 35), new Color(70, 70, 70), new Color(105, 105, 105),
                new Color(145, 145, 145), new Color(180, 180, 180), new Color(215, 215, 215), Color.WHITE,
                new Color(90, 55, 30), new Color(120, 80, 45), new Color(160, 110, 65), new Color(195, 145, 90),
                new Color(225, 180, 120), new Color(240, 210, 165), new Color(245, 235, 210), new Color(245, 241, 232)
        };
    }

    private void openColorChooser() {
        Color chosen = JColorChooser.showDialog(
                this,
                "Обери колір для вишивки",
                selectedColor
        );

        if (chosen != null) {
            selectedColor = chosen;
            eraserMode = false;
            cloneMode = false;
            moveMode = false;

            if (cloneCheckBox != null) {
                cloneCheckBox.setSelected(false);
            }

            if (moveCheckBox != null) {
                moveCheckBox.setSelected(false);
            }

            if (statusLabel != null) {
                statusLabel.setText("Обрано власний колір");
            }
        }
    }

    private JLabel rightTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.BLACK);
        return label;
    }

    private JButton rightButton(String text) {
        JButton button = new JButton(text);

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(borderBlue, 2));

        return button;
    }

    private JCheckBox automationCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);

        checkBox.setFocusPainted(false);
        checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBox.setBackground(Color.WHITE);
        checkBox.setForeground(Color.BLACK);
        checkBox.setFont(new Font("Arial", Font.BOLD, 12));
        checkBox.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        return checkBox;
    }

    private void updateSymmetryStatus() {
        if (statusLabel == null) {
            return;
        }

        if (horizontalSymmetry && verticalSymmetry) {
            statusLabel.setText("Увімкнено симетрію: горизонтальна і вертикальна");
        } else if (horizontalSymmetry) {
            statusLabel.setText("Увімкнено горизонтальну симетрію");
        } else if (verticalSymmetry) {
            statusLabel.setText("Увімкнено вертикальну симетрію");
        } else {
            statusLabel.setText("Симетрію вимкнено");
        }
    }

    private void createStatusBar() {
        statusLabel = new JLabel("Готово");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setPreferredSize(new Dimension(AppWindow.WINDOW_W, 28));
        statusLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderBlue));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void saveState() {
        if (cells == null) {
            return;
        }

        undoStack.push(copyCells());

        if (undoStack.size() > 100) {
            undoStack.remove(0);
        }
    }

    private Color[][] copyCells() {
        Color[][] copy = new Color[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                copy[r][c] = cells[r][c];
            }
        }

        return copy;
    }

    private void undoAction() {
        if (undoStack.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("Немає дій для повернення");
            }
            return;
        }

        cells = undoStack.pop();

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }

        if (statusLabel != null) {
            statusLabel.setText("Дію скасовано");
        }
    }

    private void changeGridSize() {
        String value = JOptionPane.showInputDialog(
                this,
                "Введи розмір сітки у форматі: рядки x стовпці\nНаприклад: 50x52",
                rows + "x" + cols
        );

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        try {
            value = value.toLowerCase().replace(" ", "");
            String[] parts = value.split("x");

            int newRows = Integer.parseInt(parts[0]);
            int newCols = Integer.parseInt(parts[1]);

            if (newRows < 10 || newCols < 10 || newRows > 120 || newCols > 180) {
                JOptionPane.showMessageDialog(this, "Розмір має бути від 10 до 120 рядків і до 180 стовпців.");
                return;
            }

            saveState();

            rows = newRows;
            cols = newCols;

            createCells();

            gridButton.setText("Сітка: " + rows + " x " + cols);

            canvasPanel.setPreferredSize(new Dimension(cols * cellSize + 1, rows * cellSize + 1));
            canvasPanel.revalidate();
            canvasPanel.repaint();

            if (statusLabel != null) {
                statusLabel.setText("Сітка змінена: " + rows + " x " + cols);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Неправильний формат. Приклад: 50x52");
        }
    }

    private void clearCanvas() {
        saveState();

        createCells();

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }

        if (statusLabel != null) {
            statusLabel.setText("Полотно очищено");
        }
    }

    private void generateTextPattern() {
        String name = JOptionPane.showInputDialog(this, "Введи ім'я або слово:");

        if (name == null || name.trim().isEmpty()) {
            return;
        }

        saveState();

        String text = name.trim().toUpperCase();

        drawNameAsOneCombinedOrnament(text);

        canvasPanel.repaint();

        if (statusLabel != null) {
            statusLabel.setText("Згенеровано один орнамент з імені: " + name);
        }
    }

    private void drawNameAsOneCombinedOrnament(String text) {
        EmbroideryModel model = new EmbroideryModel(rows, cols);
        NamePatternGenerator.generateName(model, text);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = model.getCell(r, c);
            }
        }
    }

    private int getLetterShiftRow(int index) {
        int[] shifts = {0, -2, 2, -1, 1, -3, 3};
        return shifts[index % shifts.length];
    }

    private int getLetterShiftCol(int index) {
        int[] shifts = {0, 2, -2, 4, -4, 1, -1};
        return shifts[index % shifts.length];
    }

    private void mergeLetterIntoOneOrnament(Color[][] result, EmbroideryModel letterModel, int shiftRow, int shiftCol, int letterIndex) {
        int size = result.length;

        for (int r = 0; r < letterModel.getRows(); r++) {
            for (int c = 0; c < letterModel.getCols(); c++) {
                Color color = letterModel.getCell(r, c);

                if (color != null && !Color.WHITE.equals(color)) {
                    int targetRow = r + shiftRow;
                    int targetCol = c + shiftCol;

                    if (targetRow >= 0 && targetRow < size && targetCol >= 0 && targetCol < size) {
                        result[targetRow][targetCol] = chooseNameColor(color, letterIndex);
                    }
                }
            }
        }
    }

    private Color chooseNameColor(Color original, int letterIndex) {
        Color red = new Color(190, 0, 0);
        Color darkBlue = new Color(25, 80, 90);
        Color yellow = new Color(230, 170, 40);
        Color green = new Color(50, 130, 70);

        if (letterIndex % 4 == 0) {
            return red;
        }

        if (letterIndex % 4 == 1) {
            return darkBlue;
        }

        if (letterIndex % 4 == 2) {
            return yellow;
        }

        return green;
    }

    private void makeOrnamentSymmetric(Color[][] ornament) {
        int size = ornament.length;

        Color[][] copy = new Color[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                copy[r][c] = ornament[r][c];
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Color color = copy[r][c];

                if (color != null && !Color.WHITE.equals(color)) {
                    ornament[r][c] = color;
                    ornament[r][size - 1 - c] = color;
                    ornament[size - 1 - r][c] = color;
                    ornament[size - 1 - r][size - 1 - c] = color;
                }
            }
        }
    }

    private void drawLetter(String letter) {
        saveState();

        EmbroideryModel model = new EmbroideryModel(25, 25);
        NamePatternGenerator.generateName(model, letter);

        int startRow = rows / 2 - 12;
        int startCol = cols / 2 - 12;

        for (int r = 0; r < model.getRows(); r++) {
            for (int c = 0; c < model.getCols(); c++) {
                Color color = model.getCell(r, c);

                if (color != null && !Color.WHITE.equals(color)) {
                    putCell(startRow + r, startCol + c, color);
                }
            }
        }

        canvasPanel.repaint();

        if (statusLabel != null) {
            statusLabel.setText("Додано літеру: " + letter);
        }
    }

    private void drawSimplePattern(String name) {
        saveState();

        int centerR = rows / 2;
        int centerC = cols / 2;

        if (name.equals("Піксель")) {
            cells[centerR][centerC] = selectedColor;
        } else if (name.equals("Ромб")) {
            drawDiamond(centerR, centerC);
        } else if (name.equals("Хрест")) {
            drawCross(centerR, centerC);
        } else if (name.equals("Дерево життя")) {
            drawTree(centerR, centerC);
        } else if (name.equals("Зірка")) {
            drawStar(centerR, centerC);
        } else if (name.equals("Калина")) {
            drawKalyna(centerR, centerC);
        }

        canvasPanel.repaint();

        if (statusLabel != null) {
            statusLabel.setText("Додано елемент: " + name);
        }
    }

    private void drawDiamond(int r, int c) {
        for (int i = -5; i <= 5; i++) {
            putCellWithSymmetry(r + i, c + Math.abs(i) - 5, new Color(190, 0, 0));
            putCellWithSymmetry(r + i, c - Math.abs(i) + 5, new Color(190, 0, 0));
        }

        putCellWithSymmetry(r, c, new Color(230, 170, 40));
    }

    private void drawCross(int r, int c) {
        for (int i = -5; i <= 5; i++) {
            putCellWithSymmetry(r, c + i, selectedColor);
            putCellWithSymmetry(r + i, c, selectedColor);
        }
    }

    private void drawTree(int r, int c) {
        for (int i = 0; i < 8; i++) {
            putCellWithSymmetry(r + i, c, new Color(60, 120, 60));
        }

        for (int i = -4; i <= 4; i++) {
            putCellWithSymmetry(r + 2, c + i, new Color(60, 120, 60));
            putCellWithSymmetry(r + 4, c + i, new Color(60, 120, 60));
        }
    }

    private void drawStar(int r, int c) {
        for (int i = -5; i <= 5; i++) {
            putCellWithSymmetry(r, c + i, selectedColor);
            putCellWithSymmetry(r + i, c, selectedColor);
            putCellWithSymmetry(r + i, c + i, selectedColor);
            putCellWithSymmetry(r + i, c - i, selectedColor);
        }
    }

    private void drawKalyna(int r, int c) {
        for (int i = 0; i < 5; i++) {
            putCellWithSymmetry(r + i, c, new Color(60, 120, 60));
        }

        putCellWithSymmetry(r, c - 2, new Color(190, 0, 0));
        putCellWithSymmetry(r + 1, c + 2, new Color(190, 0, 0));
        putCellWithSymmetry(r + 3, c - 2, new Color(190, 0, 0));
        putCellWithSymmetry(r + 4, c + 2, new Color(190, 0, 0));
    }

    private void putCell(int r, int c, Color color) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            cells[r][c] = color;
        }
    }

    private void putCellWithSymmetry(int r, int c, Color color) {
        putCell(r, c, color);

        // Горизонтальна вісь — дзеркалить вгору/вниз
        if (horizontalSymmetry) {
            putCell(rows - 1 - r, c, color);
        }

        // Вертикальна вісь — дзеркалить ліво/право
        if (verticalSymmetry) {
            putCell(r, cols - 1 - c, color);
        }

        if (horizontalSymmetry && verticalSymmetry) {
            putCell(rows - 1 - r, cols - 1 - c, color);
        }
    }

    private void duplicateRight() {
        saveState();

        Color[][] copy = copyCells();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (copy[r][c] != null && !Color.WHITE.equals(copy[r][c])) {
                    int mirrorCol = cols - 1 - c;
                    cells[r][mirrorCol] = copy[r][c];
                }
            }
        }

        canvasPanel.repaint();

        if (statusLabel != null) {
            statusLabel.setText("Орнамент продубльовано вправо/вліво");
        }
    }

    private void duplicateDown() {
        saveState();

        Color[][] copy = copyCells();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (copy[r][c] != null && !Color.WHITE.equals(copy[r][c])) {
                    int mirrorRow = rows - 1 - r;
                    cells[mirrorRow][c] = copy[r][c];
                }
            }
        }

        canvasPanel.repaint();

        if (statusLabel != null) {
            statusLabel.setText("Орнамент продубльовано вгору/вниз");
        }
    }

    private void savePng() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("ornament.png"));

            int result = chooser.showSaveDialog(this);

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            BufferedImage image = new BufferedImage(
                    cols * cellSize + 1,
                    rows * cellSize + 1,
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g2 = image.createGraphics();
            canvasPanel.paintToImage(g2);
            g2.dispose();

            ImageIO.write(image, "png", chooser.getSelectedFile());

            if (statusLabel != null) {
                statusLabel.setText("PNG збережено");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Не вдалося зберегти PNG");
        }
    }

    private void openPng() {
        JFileChooser chooser = new JFileChooser();

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            BufferedImage image = ImageIO.read(chooser.getSelectedFile());

            if (image == null) {
                JOptionPane.showMessageDialog(this, "Не вдалося відкрити PNG.");
                return;
            }

            saveState();

            int newCols = Math.max(10, Math.min(180, image.getWidth() / cellSize));
            int newRows = Math.max(10, Math.min(120, image.getHeight() / cellSize));

            rows = newRows;
            cols = newCols;

            cells = new Color[rows][cols];

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int sampleX = Math.min(image.getWidth() - 1, c * cellSize + cellSize / 2);
                    int sampleY = Math.min(image.getHeight() - 1, r * cellSize + cellSize / 2);

                    Color pixel = new Color(image.getRGB(sampleX, sampleY), true);

                    if (pixel.getAlpha() < 40 || isAlmostWhite(pixel)) {
                        cells[r][c] = Color.WHITE;
                    } else {
                        cells[r][c] = new Color(pixel.getRed(), pixel.getGreen(), pixel.getBlue());
                    }
                }
            }

            gridButton.setText("Сітка: " + rows + " x " + cols);

            canvasPanel.setPreferredSize(new Dimension(cols * cellSize + 1, rows * cellSize + 1));
            canvasPanel.revalidate();
            canvasPanel.repaint();

            if (statusLabel != null) {
                statusLabel.setText("PNG відкрито");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Помилка під час відкриття PNG.");
        }
    }

    private boolean isAlmostWhite(Color color) {
        return color.getRed() > 240 &&
                color.getGreen() > 240 &&
                color.getBlue() > 240;
    }

    private void saveToConstructorHistory() {
        String name = JOptionPane.showInputDialog(
                this,
                "Введи назву роботи:",
                "Мій орнамент " + (savedDesigns.size() + 1)
        );

        if (name == null || name.trim().isEmpty()) {
            return;
        }

        SavedDesign design = new SavedDesign(
                name.trim(),
                rows,
                cols,
                copyCells()
        );

        savedDesigns.add(design);
        saveHistoryToFile();

        if (statusLabel != null) {
            statusLabel.setText("Роботу збережено в історію конструктора: " + name);
        }

        JOptionPane.showMessageDialog(this, "Роботу збережено в історію конструктора.");
    }

    private void openConstructorHistory() {
        if (savedDesigns.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Історія конструктора поки порожня.");
            return;
        }

        JDialog dialog = new JDialog(this, "Історія конструктора", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        DefaultListModel<SavedDesign> listModel = new DefaultListModel<>();
        for (SavedDesign design : savedDesigns) {
            listModel.addElement(design);
        }

        JList<SavedDesign> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new Font("Arial", Font.PLAIN, 15));
        list.setFixedCellHeight(30);
        list.setSelectedIndex(0);
        list.setCellRenderer((jList, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.name);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setBackground(isSelected ? headerBlueColor() : Color.WHITE);
            label.setForeground(Color.BLACK);
            return label;
        });

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
        scrollPane.setPreferredSize(new Dimension(390, 260));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JButton openButton = historyDialogButton("Відкрити");
        JButton deleteButton = historyDialogButton("Видалити");
        JButton clearButton = historyDialogButton("Очистити все");
        JButton closeButton = historyDialogButton("Закрити");

        openButton.addActionListener(e -> {
            SavedDesign selected = list.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog, "Спочатку обери роботу.");
                return;
            }
            loadSavedDesign(selected);
            dialog.dispose();
        });

        deleteButton.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(dialog, "Спочатку обери роботу для видалення.");
                return;
            }

            SavedDesign selected = listModel.get(index);
            int answer = JOptionPane.showConfirmDialog(
                    dialog,
                    "Видалити з історії: " + selected.name + "?",
                    "Підтвердження",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            savedDesigns.remove(selected);
            listModel.remove(index);
            saveHistoryToFile();

            if (listModel.isEmpty()) {
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Історію очищено.");
                return;
            }

            list.setSelectedIndex(Math.min(index, listModel.size() - 1));
        });

        clearButton.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(
                    dialog,
                    "Точно очистити всю історію конструктора?",
                    "Підтвердження",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            savedDesigns.clear();
            listModel.clear();
            saveHistoryToFile();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Історію очищено.");
        });

        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(openButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);

        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        wrapper.add(new JLabel("Обери роботу з історії:", SwingConstants.LEFT), BorderLayout.NORTH);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(wrapper, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JButton historyDialogButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(borderBlue, 2));
        return button;
    }

    private Color headerBlueColor() {
        return new Color(226, 241, 252);
    }

    private void loadSavedDesign(SavedDesign design) {
        saveState();

        rows = design.rows;
        cols = design.cols;

        cells = new Color[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = design.cells[r][c];
            }
        }

        gridButton.setText("Сітка: " + rows + " x " + cols);

        updateCanvasSize();

        if (statusLabel != null) {
            statusLabel.setText("Відкрито з історії конструктора: " + design.name);
        }
    }

    private void saveHistoryToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(historyFile))) {
            out.writeObject(savedDesigns);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Не вдалося зберегти історію конструктора.");
        }
    }

    @SuppressWarnings("unchecked")
    private void loadHistoryFromFile() {
        if (!historyFile.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(historyFile))) {
            Object object = in.readObject();

            if (object instanceof java.util.List<?>) {
                savedDesigns.clear();
                savedDesigns.addAll((java.util.List<SavedDesign>) object);
            }
        } catch (Exception ex) {
            savedDesigns.clear();
        }
    }

    private void startCloneSelection(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return;
        }

        selectStartRow = row;
        selectStartCol = col;
        selectEndRow = row;
        selectEndCol = col;

        selectingCloneZone = true;
        cloningNow = false;
        cloneZoneReady = false;
        clonePattern = null;

        if (statusLabel != null) {
            statusLabel.setText("Виділяй зону для клонування");
        }

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void updateCloneSelection(int row, int col) {
        if (!selectingCloneZone) {
            return;
        }

        selectEndRow = Math.max(0, Math.min(rows - 1, row));
        selectEndCol = Math.max(0, Math.min(cols - 1, col));

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void finishCloneSelection() {
        if (!selectingCloneZone) {
            return;
        }

        selectingCloneZone = false;

        int minRow = Math.min(selectStartRow, selectEndRow);
        int maxRow = Math.max(selectStartRow, selectEndRow);
        int minCol = Math.min(selectStartCol, selectEndCol);
        int maxCol = Math.max(selectStartCol, selectEndCol);

        cloneRows = maxRow - minRow + 1;
        cloneCols = maxCol - minCol + 1;

        clonePattern = new Color[cloneRows][cloneCols];

        boolean hasSomething = false;

        for (int r = 0; r < cloneRows; r++) {
            for (int c = 0; c < cloneCols; c++) {
                Color color = cells[minRow + r][minCol + c];

                if (color != null && !Color.WHITE.equals(color)) {
                    clonePattern[r][c] = color;
                    hasSomething = true;
                } else {
                    clonePattern[r][c] = Color.WHITE;
                }
            }
        }

        if (!hasSomething) {
            clonePattern = null;
            cloneZoneReady = false;

            if (statusLabel != null) {
                statusLabel.setText("У виділеній зоні немає орнаменту");
            }

            if (canvasPanel != null) {
                canvasPanel.repaint();
            }

            return;
        }

        cloneOffsetRow = 0;
        cloneOffsetCol = 0;
        cloneZoneReady = true;

        if (statusLabel != null) {
            statusLabel.setText("Зону вибрано. Тепер натисни місце, куди поставити копію");
        }

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void startCloneMove(int row, int col) {
        if (!cloneZoneReady || clonePattern == null) {
            return;
        }

        cloneTargetRow = row;
        cloneTargetCol = col;
        cloningNow = true;

        if (statusLabel != null) {
            statusLabel.setText("Перетягни копію виділеної зони");
        }

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void updateCloneTarget(int row, int col) {
        if (!cloningNow) {
            return;
        }

        cloneTargetRow = row;
        cloneTargetCol = col;

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void finishClone() {
        if (!cloningNow || clonePattern == null) {
            cloningNow = false;
            return;
        }

        saveState();

        int startRow = cloneTargetRow - cloneOffsetRow;
        int startCol = cloneTargetCol - cloneOffsetCol;

        for (int r = 0; r < cloneRows; r++) {
            for (int c = 0; c < cloneCols; c++) {
                Color color = clonePattern[r][c];

                if (color != null && !Color.WHITE.equals(color)) {
                    putCell(startRow + r, startCol + c, color);
                }
            }
        }

        cloningNow = false;

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }

        if (statusLabel != null) {
            statusLabel.setText("Копію виділеної зони вставлено");
        }
    }

    private void drawCloneSelection(Graphics2D g2) {
        if (!selectingCloneZone) {
            return;
        }

        int minRow = Math.min(selectStartRow, selectEndRow);
        int maxRow = Math.max(selectStartRow, selectEndRow);
        int minCol = Math.min(selectStartCol, selectEndCol);
        int maxCol = Math.max(selectStartCol, selectEndCol);

        int x = minCol * cellSize;
        int y = minRow * cellSize;
        int w = (maxCol - minCol + 1) * cellSize;
        int h = (maxRow - minRow + 1) * cellSize;

        Composite oldComposite = g2.getComposite();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(80, 150, 220));
        g2.fillRect(x, y, w, h);

        g2.setComposite(oldComposite);
        g2.setColor(new Color(30, 100, 180));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, w, h);
    }

    private void drawClonePreview(Graphics2D g2) {
        if (!cloningNow || clonePattern == null) {
            return;
        }

        int startRow = cloneTargetRow - cloneOffsetRow;
        int startCol = cloneTargetCol - cloneOffsetCol;

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));

        for (int r = 0; r < cloneRows; r++) {
            for (int c = 0; c < cloneCols; c++) {
                Color color = clonePattern[r][c];

                if (color != null && !Color.WHITE.equals(color)) {
                    int drawRow = startRow + r;
                    int drawCol = startCol + c;

                    if (drawRow >= 0 && drawRow < rows && drawCol >= 0 && drawCol < cols) {
                        g2.setColor(color);
                        g2.fillRect(
                                drawCol * cellSize + 1,
                                drawRow * cellSize + 1,
                                cellSize - 1,
                                cellSize - 1
                        );
                    }
                }
            }
        }

        g2.setComposite(oldComposite);
    }

    private void startMoveSelection(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return;
        }

        moveStartRow = row;
        moveStartCol = col;
        moveEndRow = row;
        moveEndCol = col;

        selectingMoveZone = true;
        movingNow = false;
        moveZoneReady = false;
        movePattern = null;

        if (statusLabel != null) {
            statusLabel.setText("Виділяй деталь для переміщення");
        }

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void updateMoveSelection(int row, int col) {
        if (!selectingMoveZone) {
            return;
        }

        moveEndRow = Math.max(0, Math.min(rows - 1, row));
        moveEndCol = Math.max(0, Math.min(cols - 1, col));

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void finishMoveSelection() {
        if (!selectingMoveZone) {
            return;
        }

        selectingMoveZone = false;

        int minRow = Math.min(moveStartRow, moveEndRow);
        int maxRow = Math.max(moveStartRow, moveEndRow);
        int minCol = Math.min(moveStartCol, moveEndCol);
        int maxCol = Math.max(moveStartCol, moveEndCol);

        moveRows = maxRow - minRow + 1;
        moveCols = maxCol - minCol + 1;

        movePattern = new Color[moveRows][moveCols];

        boolean hasSomething = false;

        for (int r = 0; r < moveRows; r++) {
            for (int c = 0; c < moveCols; c++) {
                Color color = cells[minRow + r][minCol + c];

                if (color != null && !Color.WHITE.equals(color)) {
                    movePattern[r][c] = color;
                    hasSomething = true;
                } else {
                    movePattern[r][c] = Color.WHITE;
                }
            }
        }

        if (!hasSomething) {
            movePattern = null;
            moveZoneReady = false;

            if (statusLabel != null) {
                statusLabel.setText("У виділеній зоні немає деталі");
            }

            if (canvasPanel != null) {
                canvasPanel.repaint();
            }

            return;
        }

        moveOriginalRow = minRow;
        moveOriginalCol = minCol;
        moveTargetRow = minRow;
        moveTargetCol = minCol;

        moveZoneReady = true;

        if (statusLabel != null) {
            statusLabel.setText("Деталь вибрана. Тепер натисни і перетягни її");
        }

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void startMoveSelected(int row, int col) {
        if (!moveZoneReady || movePattern == null) {
            return;
        }

        moveTargetRow = row;
        moveTargetCol = col;
        movingNow = true;

        if (statusLabel != null) {
            statusLabel.setText("Перетягни вибрану деталь");
        }

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void updateMoveSelected(int row, int col) {
        if (!movingNow) {
            return;
        }

        moveTargetRow = Math.max(0, Math.min(rows - 1, row));
        moveTargetCol = Math.max(0, Math.min(cols - 1, col));

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }
    }

    private void finishMoveSelected() {
        if (!movingNow || movePattern == null) {
            movingNow = false;
            return;
        }

        saveState();

        for (int r = 0; r < moveRows; r++) {
            for (int c = 0; c < moveCols; c++) {
                Color color = movePattern[r][c];

                if (color != null && !Color.WHITE.equals(color)) {
                    int oldRow = moveOriginalRow + r;
                    int oldCol = moveOriginalCol + c;

                    if (oldRow >= 0 && oldRow < rows && oldCol >= 0 && oldCol < cols) {
                        cells[oldRow][oldCol] = Color.WHITE;
                    }
                }
            }
        }

        for (int r = 0; r < moveRows; r++) {
            for (int c = 0; c < moveCols; c++) {
                Color color = movePattern[r][c];

                if (color != null && !Color.WHITE.equals(color)) {
                    int newRow = moveTargetRow + r;
                    int newCol = moveTargetCol + c;

                    if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                        cells[newRow][newCol] = color;
                    }
                }
            }
        }

        movingNow = false;
        moveZoneReady = false;
        movePattern = null;

        if (canvasPanel != null) {
            canvasPanel.repaint();
        }

        if (statusLabel != null) {
            statusLabel.setText("Деталь переміщено");
        }
    }

    private void drawMoveSelection(Graphics2D g2) {
        if (!selectingMoveZone) {
            return;
        }

        int minRow = Math.min(moveStartRow, moveEndRow);
        int maxRow = Math.max(moveStartRow, moveEndRow);
        int minCol = Math.min(moveStartCol, moveEndCol);
        int maxCol = Math.max(moveStartCol, moveEndCol);

        int x = minCol * cellSize;
        int y = minRow * cellSize;
        int w = (maxCol - minCol + 1) * cellSize;
        int h = (maxRow - minRow + 1) * cellSize;

        Composite oldComposite = g2.getComposite();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(160, 120, 220));
        g2.fillRect(x, y, w, h);

        g2.setComposite(oldComposite);
        g2.setColor(new Color(90, 40, 160));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, w, h);
    }

    private void drawMovePreview(Graphics2D g2) {
        if (!movingNow || movePattern == null) {
            return;
        }

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));

        for (int r = 0; r < moveRows; r++) {
            for (int c = 0; c < moveCols; c++) {
                Color color = movePattern[r][c];

                if (color != null && !Color.WHITE.equals(color)) {
                    int drawRow = moveTargetRow + r;
                    int drawCol = moveTargetCol + c;

                    if (drawRow >= 0 && drawRow < rows && drawCol >= 0 && drawCol < cols) {
                        g2.setColor(color);
                        g2.fillRect(
                                drawCol * cellSize + 1,
                                drawRow * cellSize + 1,
                                cellSize - 1,
                                cellSize - 1
                        );
                    }
                }
            }
        }

        g2.setComposite(oldComposite);
    }

    private static class SavedDesign implements Serializable {
        private static final long serialVersionUID = 1L;

        String name;
        int rows;
        int cols;
        Color[][] cells;

        SavedDesign(String name, int rows, int cols, Color[][] cells) {
            this.name = name;
            this.rows = rows;
            this.cols = cols;
            this.cells = cells;
        }
    }

    private class CanvasPanel extends JPanel {
        private boolean drawingNow = false;

        public CanvasPanel() {
            setBackground(Color.WHITE);

            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int col = e.getX() / cellSize;
                    int row = e.getY() / cellSize;

                    if (moveMode) {
                        if (!moveZoneReady) {
                            startMoveSelection(row, col);
                        } else {
                            startMoveSelected(row, col);
                        }
                    } else if (cloneMode) {
                        if (!cloneZoneReady) {
                            startCloneSelection(row, col);
                        } else {
                            startCloneMove(row, col);
                        }
                    } else {
                        if (!drawingNow) {
                            saveState();
                            drawingNow = true;
                        }

                        paintCell(e);
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    int col = e.getX() / cellSize;
                    int row = e.getY() / cellSize;

                    if (moveMode) {
                        if (selectingMoveZone) {
                            updateMoveSelection(row, col);
                        } else if (movingNow) {
                            updateMoveSelected(row, col);
                        }
                    } else if (cloneMode) {
                        if (selectingCloneZone) {
                            updateCloneSelection(row, col);
                        } else if (cloningNow) {
                            updateCloneTarget(row, col);
                        }
                    } else {
                        paintCell(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (moveMode) {
                        if (selectingMoveZone) {
                            finishMoveSelection();
                        } else if (movingNow) {
                            finishMoveSelected();
                        }
                    } else if (cloneMode) {
                        if (selectingCloneZone) {
                            finishCloneSelection();
                        } else if (cloningNow) {
                            finishClone();
                        }
                    }

                    drawingNow = false;
                }
            };

            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        private void paintCell(MouseEvent e) {
            int col = e.getX() / cellSize;
            int row = e.getY() / cellSize;

            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                Color color;

                if (eraserMode) {
                    color = Color.WHITE;
                } else {
                    color = selectedColor;
                }

                putCellWithSymmetry(row, col, color);

                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintToImage((Graphics2D) g);
            drawCloneSelection((Graphics2D) g);
            drawClonePreview((Graphics2D) g);
            drawMoveSelection((Graphics2D) g);
            drawMovePreview((Graphics2D) g);
        }

        public void paintToImage(Graphics2D g2) {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, cols * cellSize + 1, rows * cellSize + 1);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Color color = cells[r][c];

                    if (color != null && !Color.WHITE.equals(color)) {
                        g2.setColor(color);
                        g2.fillRect(c * cellSize + 1, r * cellSize + 1, cellSize - 1, cellSize - 1);
                    }
                }
            }

            if (!hideGrid) {
                g2.setColor(new Color(210, 210, 210));

                for (int r = 0; r <= rows; r++) {
                    g2.drawLine(0, r * cellSize, cols * cellSize, r * cellSize);
                }

                for (int c = 0; c <= cols; c++) {
                    g2.drawLine(c * cellSize, 0, c * cellSize, rows * cellSize);
                }
            }
        }
    }
}