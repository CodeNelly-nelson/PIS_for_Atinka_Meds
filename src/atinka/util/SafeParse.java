package atinka.util;

/** Safe numeric parsing with sentinels (no java.util). */
public final class SafeParse {
    private SafeParse() {}

    public static boolean isBlank(String s){ return s == null || s.trim().length() == 0; }

    public static int toInt(String s, int fallback) {
        if (s == null) return fallback;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    public static double toDouble(String s, double fallback) {
        if (s == null) return fallback;
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return fallback; }
    }
}
