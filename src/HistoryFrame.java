import javax.swing.*;
import java.awt.*;

public class HistoryFrame extends JFrame {
    private final Color headerBlue = new Color(226, 241, 252);
    private final Color background = new Color(238, 238, 238);
    private final Color brown = new Color(125, 55, 50);
    private final Color borderBlue = new Color(175, 205, 230);

    public HistoryFrame() {
        super("Вишиванка власноруч — Попкова Кристина. ІПЗ-1");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        AppWindow.setup(this);

        setLayout(new BorderLayout());

        createTopPanel();
        createContentPanel();
    }

    private void createTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(headerBlue);
        top.setPreferredSize(new Dimension(AppWindow.WINDOW_W, 145));
        top.setBorder(BorderFactory.createEmptyBorder(20, 55, 20, 55));

        JPanel leftEmpty = new JPanel();
        leftEmpty.setOpaque(false);
        leftEmpty.setPreferredSize(new Dimension(210, 105));

        JLabel title = new JLabel("Історія української вишивки", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 34));
        title.setForeground(brown);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 9));
        buttons.setOpaque(false);
        buttons.setPreferredSize(new Dimension(180, 105));

        buttons.add(navButton("На головну →", () -> {
            AppWindow.open(this, new HomeFrame());
        }));

        buttons.add(navButton("Конструктор →", () -> {
            AppWindow.open(this, new EmbroideryFrame());
        }));

        buttons.add(navButton("Орнаменти →", () -> {
            AppWindow.open(this, new OrnamentsFrame());
        }));

        top.add(leftEmpty, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
    }

    private JButton navButton(String text, Runnable action) {
        JButton button = new JButton(text);

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(headerBlue);
        button.setForeground(brown);
        button.setFont(new Font("Georgia", Font.BOLD, 16));
        button.setBorder(BorderFactory.createLineBorder(new Color(165, 155, 170), 2));

        button.addActionListener(e -> action.run());

        return button;
    }

    private void createContentPanel() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(background);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);

        int cardWidth = 900;
        int row = 0;

        addVerticalSpace(content, gbc, row++, 42);

        addTitle(content, gbc, row++, "Історія регіонів та символів української вишивки");
        addVerticalSpace(content, gbc, row++, 14);
        addCard(content, gbc, row++, createTextCard(
                cardWidth,
                105,
                "<html><div style='text-align:center;'>" +
                        "У цьому розділі зібрано головне про походження вишиванки, значення кольорів, символів,<br>" +
                        "регіональні особливості та традиційні техніки вишивки." +
                        "</div></html>",
                true
        ));

        addVerticalSpace(content, gbc, row++, 28);

        addTitle(content, gbc, row++, "Історія вишиванки");
        addVerticalSpace(content, gbc, row++, 14);
        addCard(content, gbc, row++, createTextCard(
                cardWidth,
                205,
                "<html>" +
                        "Витоки української вишиванки сягають часів Київської Русі. " +
                        "Спочатку вишита сорочка була частиною повсякденного одягу, " +
                        "але навіть тоді орнаменти мали особливе значення. " +
                        "З часом вишиванка стала не просто елементом гардероба, " +
                        "а символом роду, пам’яті, захисту та української ідентичності.<br><br>" +

                        "У XVIII–XIX століттях, коли українська культура часто зазнавала утисків, " +
                        "вишиванка допомагала зберігати національну самобутність. " +
                        "Люди передавали сорочки з покоління в покоління, а кожен регіон " +
                        "створював власні кольори, техніки та мотиви." +
                        "</html>",
                false
        ));

        addVerticalSpace(content, gbc, row++, 28);

        addTitle(content, gbc, row++, "Символіка кольорів");
        addVerticalSpace(content, gbc, row++, 14);
        addCard(content, gbc, row++, createTextCard(
                cardWidth,
                190,
                "<html>" +
                        "<b>Червоний</b> — любов, життя, енергія та захист.<br><br>" +
                        "<b>Чорний</b> — земля, пам’ять роду, сила та мудрість.<br><br>" +
                        "<b>Білий</b> — чистота, світло, гармонія та духовність.<br><br>" +
                        "<b>Синій</b> — спокій, вода, небо та душевна рівновага." +
                        "</html>",
                false
        ));

        addVerticalSpace(content, gbc, row++, 28);

        addTitle(content, gbc, row++, "Символи в орнаментах");
        addVerticalSpace(content, gbc, row++, 14);
        addCard(content, gbc, row++, createTextCard(
                cardWidth,
                230,
                "<html>" +
                        "<b>Ромб</b> — добробут, родючість і продовження роду.<br><br>" +
                        "<b>Хрест</b> — оберіг і захист від злих сил.<br><br>" +
                        "<b>Зірка</b> — гармонія, світло та життєва енергія.<br><br>" +
                        "<b>Птахи</b> — щастя, добрі новини та духовність.<br><br>" +
                        "<b>Дерево життя</b> — сила роду і зв’язок поколінь." +
                        "</html>",
                false
        ));

        addVerticalSpace(content, gbc, row++, 28);

        addTitle(content, gbc, row++, "Регіональні особливості");
        addVerticalSpace(content, gbc, row++, 14);
        addCard(content, gbc, row++, createTextCard(
                cardWidth,
                230,
                "<html>" +
                        "Кожен регіон України має власні особливості вишивки. " +
                        "На Полтавщині часто використовували ніжні світлі кольори, " +
                        "на Гуцульщині — яскраві контрастні поєднання, " +
                        "а на Поділлі — насичені геометричні орнаменти.<br><br>" +

                        "Саме завдяки цій різноманітності українська вишивка стала " +
                        "неповторним культурним явищем, у якому поєднуються краса, " +
                        "історія, символіка та пам’ять народу." +
                        "</html>",
                false
        ));

        addVerticalSpace(content, gbc, row++, 45);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(background);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void addVerticalSpace(JPanel content, GridBagConstraints gbc, int row, int height) {
        gbc.gridy = row;
        content.add(Box.createRigidArea(new Dimension(1, height)), gbc);
    }

    private void addTitle(JPanel content, GridBagConstraints gbc, int row, String text) {
        gbc.gridy = row;

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Georgia", Font.BOLD, 27));
        label.setForeground(brown);
        label.setPreferredSize(new Dimension(950, 38));

        content.add(label, gbc);
    }

    private void addCard(JPanel content, GridBagConstraints gbc, int row, JPanel card) {
        gbc.gridy = row;
        content.add(card, gbc);
    }

    private JPanel createTextCard(int width, int height, String text, boolean centered) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderBlue, 2),
                BorderFactory.createEmptyBorder(22, 26, 22, 26)
        ));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(Color.BLACK);

        if (centered) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }

        card.add(label, BorderLayout.CENTER);

        card.setPreferredSize(new Dimension(width, height));
        card.setMinimumSize(new Dimension(width, height));
        card.setMaximumSize(new Dimension(width, height));

        return card;
    }
}