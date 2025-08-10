package atinka.util;

import static atinka.util.Ansi.*;

/** Tiny UI toolkit for the terminal: banners, toasts, tables, spinner, status. */
public final class Tui {
    private Tui() {}

    // ---- Banner / Header ----
    public static void banner(String title, String subtitle) {
        String bar = "────────────────────────────────────────────────────────";
        System.out.println(color(" " + bar, FG_CYAN));
        System.out.println(color("  " + title, BOLD, FG_CYAN));
        if (subtitle != null && subtitle.length() > 0) {
            System.out.println(color("  " + subtitle, DIM));
        }
        System.out.println(color(" " + bar, FG_CYAN));
        System.out.println();
    }

    public static void breadcrumb(String path) {
        System.out.println(color("› " + path, FG_BLUE, BOLD));
        System.out.println();
    }

    // ---- Toasts ----
    public static void toastSuccess(String msg) { toast(msg, FG_GREEN); }
    public static void toastWarn(String msg)    { toast(msg, FG_YELLOW); }
    public static void toastError(String msg)   { toast(msg, FG_RED); }
    public static void toastInfo(String msg)    { toast(msg, FG_CYAN); }

    private static void toast(String msg, String color) {
        System.out.println(color("● ", color) + msg);
    }

    // ---- Status line (like a footer hint) ----
    public static void status(String msg) {
        System.out.println();
        System.out.println(color(" " + msg, DIM));
    }

    // ---- Simple spinner animation (blocking) ----
    public static void spinner(String label, int steps, int delayMs) {
        char[] frames = new char[]{'⠋','⠙','⠹','⠸','⠼','⠴','⠦','⠧','⠇','⠏'};
        try {
            for (int i = 0; i < steps; i++) {
                char f = frames[i % frames.length];
                System.out.print("\r" + color(" " + f + " ", FG_CYAN) + label + "...");
                Thread.sleep(delayMs);
            }
        } catch (InterruptedException ignored) {}
        System.out.print("\r" + color(" ✓ ", FG_GREEN) + label + "    \n");
    }

    // ---- Table builder (fixed width columns, with optional color per row) ----
    public static void table(String[] headers, int[] widths, RowProvider rows) {
        if (headers == null || widths == null || headers.length != widths.length) {
            System.out.println("(table config error)");
            return;
        }
        // header
        StringBuilder h = new StringBuilder();
        for (int i=0;i<headers.length;i++) {
            h.append(pad(headers[i], widths[i]));
            if (i < headers.length-1) h.append("  ");
        }
        System.out.println(color(h.toString(), BOLD));

        // separator
        StringBuilder sep = new StringBuilder();
        for (int w : widths) {
            sep.append(repeat("─", w)).append("  ");
        }
        System.out.println(color(sep.toString(), DIM));

        // rows
        int r = 0;
        while (true) {
            String[] row = rows.nextRow(r);
            if (row == null) break;
            String color = rows.rowColor(r);
            StringBuilder line = new StringBuilder();
            for (int i=0;i<widths.length;i++) {
                String cell = (i < row.length ? row[i] : "");
                line.append(pad(cell, widths[i]));
                if (i < widths.length-1) line.append("  ");
            }
            if (color != null) System.out.println(Ansi.color(line.toString(), color));
            else System.out.println(line.toString());
            r++;
        }
    }

    public interface RowProvider {
        /** return null to stop */
        String[] nextRow(int index);
        /** return ANSI fg color (e.g., Ansi.FG_YELLOW) or null */
        default String rowColor(int index) { return null; }
    }

    // ---- small helpers ----
    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder(n * s.length());
        for (int i=0;i<n;i++) sb.append(s);
        return sb.toString();
    }

    private static String pad(String s, int w) {
        if (s == null) s = "";
        if (s.length() >= w) return s.substring(0, w);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < w) sb.append(' ');
        return sb.toString();
    }
}
