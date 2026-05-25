import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class NamePatternGenerator {
    private static final Color RED = new Color(180, 35, 35);
    private static final Color DARK_RED = new Color(135, 25, 28);
    private static final Color BLACK = new Color(25, 25, 25);
    private static final Color BLUE = new Color(31, 73, 81);
    private static final Color GOLD = new Color(225, 168, 65);
    private static final Color GREEN = new Color(48, 120, 86);

    private static final Map<Character, Integer> LETTER_TYPES = new HashMap<>();

    static {
        String alphabet = "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ";
        for (int i = 0; i < alphabet.length(); i++) {
            LETTER_TYPES.put(alphabet.charAt(i), i % 12);
        }

        // Підтримка латинської клавіатури, щоб можна було ввести mama, anna, a.
        LETTER_TYPES.put('A', LETTER_TYPES.get('А'));
        LETTER_TYPES.put('B', LETTER_TYPES.get('Б'));
        LETTER_TYPES.put('C', LETTER_TYPES.get('С'));
        LETTER_TYPES.put('D', LETTER_TYPES.get('Д'));
        LETTER_TYPES.put('E', LETTER_TYPES.get('Е'));
        LETTER_TYPES.put('F', LETTER_TYPES.get('Ф'));
        LETTER_TYPES.put('G', LETTER_TYPES.get('Г'));
        LETTER_TYPES.put('H', LETTER_TYPES.get('Н'));
        LETTER_TYPES.put('I', LETTER_TYPES.get('І'));
        LETTER_TYPES.put('J', LETTER_TYPES.get('Й'));
        LETTER_TYPES.put('K', LETTER_TYPES.get('К'));
        LETTER_TYPES.put('L', LETTER_TYPES.get('Л'));
        LETTER_TYPES.put('M', LETTER_TYPES.get('М'));
        LETTER_TYPES.put('N', LETTER_TYPES.get('Н'));
        LETTER_TYPES.put('O', LETTER_TYPES.get('О'));
        LETTER_TYPES.put('P', LETTER_TYPES.get('П'));
        LETTER_TYPES.put('R', LETTER_TYPES.get('Р'));
        LETTER_TYPES.put('S', LETTER_TYPES.get('С'));
        LETTER_TYPES.put('T', LETTER_TYPES.get('Т'));
        LETTER_TYPES.put('U', LETTER_TYPES.get('У'));
        LETTER_TYPES.put('V', LETTER_TYPES.get('В'));
        LETTER_TYPES.put('X', LETTER_TYPES.get('Х'));
        LETTER_TYPES.put('Y', LETTER_TYPES.get('И'));
        LETTER_TYPES.put('Z', LETTER_TYPES.get('З'));
    }

    public static void generateName(EmbroideryModel model, String text) {
        model.clear();

        String cleanText = normalizeText(text);
        if (cleanText.isEmpty()) {
            return;
        }


        NamePatternRepository.NamePattern readyPattern = NamePatternRepository.findByName(cleanText);
        if (readyPattern == null) {
            readyPattern = NamePatternRepository.findByName(toTitleCase(cleanText));
        }
        if (readyPattern != null) {
            drawRepositoryPattern(model, readyPattern.cells());
            return;
        }

       if (cleanText.length() == 1) {
            drawSingleLetterOrnament(model, cleanText.charAt(0));
            return;
        }

        // Якщо імені ще немає в готовій базі, воно автоматично додається
        // у файл name_patterns_database.txt і отримує свій постійний унікальний рисунок.
        readyPattern = NamePatternRepository.findOrCreateByName(cleanText);
        if (readyPattern != null) {
            drawRepositoryPattern(model, readyPattern.cells());
            return;
        }

        if (isMama(cleanText)) {
            drawMamaOrnament(model);
            return;
        }

        drawWordOrnament(model, cleanText);
    }

    private static String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        String upper = text.trim().toUpperCase()
                .replace('Ё', 'Е')
                .replace('Ъ', 'Ь')
                .replace('Ы', 'И')
                .replace('Э', 'Е');

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            if (Character.isLetter(ch)) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private static boolean isMama(String text) {
        return text.equals("МАМА") || text.equals("MAMA");
    }

    private static void drawSingleLetterOrnament(EmbroideryModel model, char letter) {
        int size = 11;
        size = Math.min(size, Math.min(model.getRows(), model.getCols()) - 10);
        if (size % 2 == 0) size--;
        if (size < 7) size = 7;

        int top = (model.getRows() - size) / 2;
        int left = (model.getCols() - size) / 2;
        int type = LETTER_TYPES.getOrDefault(letter, Math.abs(letter) % 12);

        // Буква A/А - саме ромб, як на прикладі.
        if (letter == 'А' || letter == 'A') {
            drawDiamondOutline(model, top, left, size, RED);
            put(model, top + size / 2, left + size / 2, GOLD);
            return;
        }

        drawSmallMotifByType(model, top, left, size, type);
    }

    private static void drawMamaOrnament(EmbroideryModel model) {
        int size = 17;
        size = fitOddSize(model, size);
        int top = (model.getRows() - size) / 2;
        int left = (model.getCols() - size) / 2;
        int mid = size / 2;

        // Центральний червоний хрест і ромб - основа слова.
        drawDiamondOutline(model, top, left, size, RED);
        drawPlusLimited(model, top, left, size, RED, 6);
        drawXLimited(model, top, left, size, RED, 4);

        // Чорні/сині квадрати по кутах, як у текстовій вишивці.
        drawBlock(model, top + mid - 5, left + mid - 5, BLUE);
        drawBlock(model, top + mid - 5, left + mid + 4, BLACK);
        drawBlock(model, top + mid + 4, left + mid - 5, BLACK);
        drawBlock(model, top + mid + 4, left + mid + 4, BLUE);

        // Жовті декоративні куточки.
        drawCornerHook(model, top + 1, left + 1, 1, 1, GOLD);
        drawCornerHook(model, top + 1, left + size - 3, 1, -1, GOLD);
        drawCornerHook(model, top + size - 3, left + 1, -1, 1, GOLD);
        drawCornerHook(model, top + size - 3, left + size - 3, -1, -1, GOLD);

        // Маленькі червоні стібки всередині, щоб орнамент був щільний, не порожній.
        putSym(model, top + mid, left + mid, 2, 2, RED);
        putSym(model, top + mid, left + mid, 2, -2, RED);
        putSym(model, top + mid, left + mid, 3, 0, RED);
        putSym(model, top + mid, left + mid, 0, 3, RED);
        put(model, top + mid, left + mid, GOLD);
    }

    private static void drawWordOrnament(EmbroideryModel model, String text) {
        int len = text.length();
        int size = 13 + Math.min(8, len);
        size = fitOddSize(model, size);

        int top = (model.getRows() - size) / 2;
        int left = (model.getCols() - size) / 2;
        int centerRow = top + size / 2;
        int centerCol = left + size / 2;
        int radius = size / 2;

        drawDiamondOutline(model, top, left, size, RED);
        drawPlusLimited(model, top, left, size, RED, Math.max(4, radius - 2));

        for (int i = 0; i < len; i++) {
            char letter = text.charAt(i);
            int type = LETTER_TYPES.getOrDefault(letter, Math.abs(letter) % 12);
            int layer = 2 + (i % Math.max(1, radius - 3));
            Color color = pickColor(type);

            switch (type % 6) {
                case 0 -> putSym(model, centerRow, centerCol, layer, 0, color);
                case 1 -> putSym(model, centerRow, centerCol, 0, layer, color);
                case 2 -> putSym(model, centerRow, centerCol, layer, layer, color);
                case 3 -> putSym(model, centerRow, centerCol, layer, -layer, color);
                case 4 -> {
                    putSym(model, centerRow, centerCol, layer, 1, color);
                    putSym(model, centerRow, centerCol, 1, layer, color);
                }
                default -> {
                    putSym(model, centerRow, centerCol, layer, 2, color);
                    putSym(model, centerRow, centerCol, 2, layer, color);
                }
            }
        }

        drawInnerCorners(model, top, left, size, BLUE);
        put(model, centerRow, centerCol, GOLD);
    }


    private static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String lower = text.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static void drawRepositoryPattern(EmbroideryModel model, int[][] pattern) {
        model.clear();
        if (pattern == null || pattern.length == 0 || pattern[0].length == 0) {
            return;
        }

        int patternRows = pattern.length;
        int patternCols = pattern[0].length;

        int safeRows = Math.max(1, model.getRows() - 8);
        int safeCols = Math.max(1, model.getCols() - 8);

        int step = 1;
        while ((patternRows + step - 1) / step > safeRows || (patternCols + step - 1) / step > safeCols) {
            step++;
        }

        int drawRows = (patternRows + step - 1) / step;
        int drawCols = (patternCols + step - 1) / step;

        int startRow = Math.max(0, (model.getRows() - drawRows) / 2);
        int startCol = Math.max(0, (model.getCols() - drawCols) / 2);

        for (int r = 0; r < patternRows; r += step) {
            for (int c = 0; c < patternCols; c += step) {
                int value = strongestCell(pattern, r, c, step);
                if (value == NamePatternRepository.EMPTY_CELL) {
                    continue;
                }

                Color color = value == NamePatternRepository.RED_CELL ? RED : BLACK;
                int row = startRow + r / step;
                int col = startCol + c / step;
                put(model, row, col, color);
            }
        }
    }

    private static int strongestCell(int[][] pattern, int row, int col, int step) {
        boolean hasBlack = false;
        boolean hasRed = false;

        for (int r = row; r < Math.min(pattern.length, row + step); r++) {
            for (int c = col; c < Math.min(pattern[r].length, col + step); c++) {
                if (pattern[r][c] == NamePatternRepository.BLACK_CELL) {
                    hasBlack = true;
                } else if (pattern[r][c] == NamePatternRepository.RED_CELL) {
                    hasRed = true;
                }
            }
        }

        if (hasBlack) {
            return NamePatternRepository.BLACK_CELL;
        }
        if (hasRed) {
            return NamePatternRepository.RED_CELL;
        }
        return NamePatternRepository.EMPTY_CELL;
    }

    private static int fitOddSize(EmbroideryModel model, int wanted) {
        int max = Math.min(model.getRows(), model.getCols()) - 10;
        int size = Math.min(wanted, max);
        if (size % 2 == 0) size--;
        if (size < 7) size = 7;
        return size;
    }

    private static Color pickColor(int type) {
        return switch (type % 5) {
            case 0 -> RED;
            case 1 -> BLUE;
            case 2 -> GOLD;
            case 3 -> DARK_RED;
            default -> BLACK;
        };
    }

    private static void drawSmallMotifByType(EmbroideryModel model, int top, int left, int size, int type) {
        switch (type % 6) {
            case 0 -> {
                drawDiamondOutline(model, top, left, size, RED);
                put(model, top + size / 2, left + size / 2, GOLD);
            }
            case 1 -> {
                drawDiamondOutline(model, top, left, size, RED);
                drawSmallDiamond(model, top, left, size, BLUE);
            }
            case 2 -> {
                drawXLimited(model, top, left, size, RED, size / 2 - 1);
                drawCornerDots(model, top, left, size, GOLD);
            }
            case 3 -> {
                drawPlusLimited(model, top, left, size, RED, size / 2 - 1);
                drawSmallDiamond(model, top, left, size, BLUE);
            }
            case 4 -> {
                drawDiamondOutline(model, top, left, size, BLUE);
                drawXLimited(model, top, left, size, RED, size / 2 - 1);
            }
            default -> {
                drawDiamondOutline(model, top, left, size, BLACK);
                drawCornerDots(model, top, left, size, RED);
                put(model, top + size / 2, left + size / 2, GOLD);
            }
        }
    }

    private static void drawDiamondOutline(EmbroideryModel model, int top, int left, int size, Color color) {
        int mid = size / 2;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (Math.abs(r - mid) + Math.abs(c - mid) == mid) {
                    put(model, top + r, left + c, color);
                }
            }
        }
    }

    private static void drawSmallDiamond(EmbroideryModel model, int top, int left, int size, Color color) {
        int mid = size / 2;
        int radius = Math.max(2, mid / 2);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (Math.abs(r - mid) + Math.abs(c - mid) == radius) {
                    put(model, top + r, left + c, color);
                }
            }
        }
    }

    private static void drawPlusLimited(EmbroideryModel model, int top, int left, int size, Color color, int length) {
        int mid = size / 2;
        for (int i = -length; i <= length; i++) {
            put(model, top + mid, left + mid + i, color);
            put(model, top + mid + i, left + mid, color);
        }
    }

    private static void drawXLimited(EmbroideryModel model, int top, int left, int size, Color color, int length) {
        int mid = size / 2;
        for (int i = -length; i <= length; i++) {
            put(model, top + mid + i, left + mid + i, color);
            put(model, top + mid + i, left + mid - i, color);
        }
    }

    private static void drawCornerDots(EmbroideryModel model, int top, int left, int size, Color color) {
        put(model, top + 1, left + 1, color);
        put(model, top + 1, left + size - 2, color);
        put(model, top + size - 2, left + 1, color);
        put(model, top + size - 2, left + size - 2, color);
    }

    private static void drawInnerCorners(EmbroideryModel model, int top, int left, int size, Color color) {
        int mid = size / 2;
        int d = Math.max(4, mid - 3);
        drawBlock(model, top + mid - d, left + mid - d, color);
        drawBlock(model, top + mid - d, left + mid + d - 1, color);
        drawBlock(model, top + mid + d - 1, left + mid - d, color);
        drawBlock(model, top + mid + d - 1, left + mid + d - 1, color);
    }

    private static void drawBlock(EmbroideryModel model, int row, int col, Color color) {
        put(model, row, col, color);
        put(model, row, col + 1, color);
        put(model, row + 1, col, color);
        put(model, row + 1, col + 1, color);
    }

    private static void drawCornerHook(EmbroideryModel model, int row, int col, int rowDir, int colDir, Color color) {
        put(model, row, col, color);
        put(model, row, col + colDir, color);
        put(model, row + rowDir, col, color);
        put(model, row + rowDir, col + colDir, color);
        put(model, row + rowDir * 2, col, color);
        put(model, row, col + colDir * 2, color);
    }

    private static void putSym(EmbroideryModel model, int centerRow, int centerCol, int dr, int dc, Color color) {
        put(model, centerRow + dr, centerCol + dc, color);
        put(model, centerRow - dr, centerCol + dc, color);
        put(model, centerRow + dr, centerCol - dc, color);
        put(model, centerRow - dr, centerCol - dc, color);
    }

    private static void put(EmbroideryModel model, int row, int col, Color color) {
        if (row >= 0 && row < model.getRows() && col >= 0 && col < model.getCols()) {
            model.setCell(row, col, color);
        }
    }
}
