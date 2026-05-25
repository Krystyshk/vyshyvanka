public enum SymmetryMode {
    NONE("Без симетрії"),
    HORIZONTAL("Горизонтальна"),
    VERTICAL("Вертикальна"),
    BOTH("Горизонтальна + вертикальна");

    private final String title;

    SymmetryMode(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}