package atinka.util;

/** Minimal ANSI color/style helpers (no external libs). */
public final class Ansi {
    private Ansi() {}

    // Reset + basic styles
    public static final String RESET = "\u001B[0m";
    public static final String BOLD  = "\u001B[1m";
    public static final String DIM   = "\u001B[2m";
    public static final String ITAL  = "\u001B[3m";
    public static final String UNDER = "\u001B[4m";

    // Colors (fg)
    public static final String FG_BLACK   = "\u001B[30m";
    public static final String FG_RED     = "\u001B[31m";
    public static final String FG_GREEN   = "\u001B[32m";
    public static final String FG_YELLOW  = "\u001B[33m";
    public static final String FG_BLUE    = "\u001B[34m";
    public static final String FG_MAGENTA = "\u001B[35m";
    public static final String FG_CYAN    = "\u001B[36m";
    public static final String FG_WHITE   = "\u001B[37m";

    // Colors (bg)
    public static final String BG_RED     = "\u001B[41m";
    public static final String BG_GREEN   = "\u001B[42m";
    public static final String BG_YELLOW  = "\u001B[43m";
    public static final String BG_BLUE    = "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN    = "\u001B[46m";
    public static final String BG_WHITE   = "\u001B[47m";

    public static String color(String s, String... codes) {
        if (s == null) s = "";
        StringBuilder sb = new StringBuilder();
        for (String c : codes) sb.append(c);
        sb.append(s).append(RESET);
        return sb.toString();
    }
}
