package gui;

public enum WindowPrefKeys {
    X("x"),
    Y("y"),
    WIDTH("width"),
    HEIGHT("height"),
    STATE("state"),
    IS_MINIMIZED("isMinimized"),
    IS_MAXIMIZED("isMaximized");

    private final String key;

    WindowPrefKeys(String key) {
        this.key = key;
    }

    public String getFullKey(String prefix) {
        return prefix + "." + key;
    }

    public static String getMainWindowPrefix() {
        return "main";
    }

    public static String getInternalWindowPrefix() {
        return "window";
    }
}