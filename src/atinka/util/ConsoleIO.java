package atinka.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * ConsoleIO: robust terminal I/O + pretty headers/borders.
 * - Auto color on capable terminals (respects NO_COLOR).
 * - Clean headers boxed with unicode; ASCII fallback if needed.
 * - Cancel-anywhere: "0" / "c" / "cancel".
 * - Themed helpers: printHeaderThemed(...) and printBadges(...)
 */
public final class ConsoleIO {
    private static final BufferedReader IN =
            new BufferedReader(new InputStreamReader(System.in));

    // Box drawing (fallback if ANSI disabled & font issues)
    private static final char H  = '─';
    private static final char V  = '│';
    private static final char TL = '┌';
    private static final char TR = '┐';
    private static final char BL = '└';
    private static final char BR = '┘';

    private ConsoleIO() {}

    // ------------------ Printing helpers ------------------

    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // portable enough: just add blank lines
                for (int i = 0; i < 60; i++) System.out.println();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {}
    }

    public static void println(String s) { System.out.println(s); }
    public static void print(String s)   { System.out.print(s);  }

    public static void hr(){
        if (Ansi.isEnabled()){
            System.out.println(Ansi.fg("blue") + repeat(H, 50) + Ansi.reset());
        } else {
            System.out.println(repeat('-', 50));
        }
    }

    /** Default header (cyan text if color available). */
    public static void printHeader(String title) {
        String t = " " + title + " ";
        int w = Math.max(42, t.length() + 6);
        String top = TL + repeat(H, w) + TR;
        String mid = V + center(t, w) + V;
        String bot = BL + repeat(H, w) + BR;

        if (Ansi.isEnabled()){
            String fg = Ansi.fg("cyan") + Ansi.bold();
            System.out.println(fg + top + Ansi.reset());
            System.out.println(fg + mid + Ansi.reset());
            System.out.println(fg + bot + Ansi.reset());
        } else {
            String aa = "+" + repeat('-', w) + "+";
            System.out.println(aa);
            System.out.println("|" + center(t, w) + "|");
            System.out.println(aa);
        }
        System.out.println();
    }

    // ------------------ THEME HELPERS ------------------

    /**
     * Themed header with chosen foreground ANSI color only (no background).
     * Example: printHeaderThemed("Drugs", "cyan");
     */
    public static void printHeaderThemed(String title, String fgName) {
        String t = " " + title + " ";
        int w = Math.max(42, t.length() + 6);
        String top = TL + repeat(H, w) + TR;
        String mid = V + center(t, w) + V;
        String bot = BL + repeat(H, w) + BR;

        if (Ansi.isEnabled()){
            if (fgName == null || fgName.trim().isEmpty()) fgName = "cyan";
            String fg = Ansi.fg(fgName) + Ansi.bold();
            System.out.println(fg + top + Ansi.reset());
            System.out.println(fg + mid + Ansi.reset());
            System.out.println(fg + bot + Ansi.reset());
        } else {
            String aa = "+" + repeat('-', w) + "+";
            System.out.println(aa);
            System.out.println("|" + center(t, w) + "|");
            System.out.println(aa);
        }
        System.out.println();
    }

    /**
     * Badge row to showcase algorithms/DS used on a screen.
     * Example: printBadges("MergeSort", "BinarySearch");
     */
    public static void printBadges(String... labels){
        if (labels == null || labels.length == 0) return;
        StringBuilder line = new StringBuilder();
        for (int i=0;i<labels.length;i++){
            String lab = labels[i] == null ? "" : labels[i];
            if (Ansi.isEnabled()){
                line.append(Ansi.fg("cyan"))
                        .append("[").append(lab).append("]")
                        .append(Ansi.reset());
            } else {
                line.append('[').append(lab).append(']');
            }
            if (i < labels.length - 1) line.append(' ');
        }
        System.out.println(line.toString());
        System.out.println();
    }

    // ------------------ Read helpers ------------------

    /** Raw line (can return empty). */
    public static String readLine(String prompt) {
        while (true) {
            try {
                if (prompt != null) print(prompt);
                String s = IN.readLine();
                if (s == null) return "";
                return s.trim();
            } catch (Exception e) {
                println(Ansi.fg("red") + "Input error, try again." + Ansi.reset());
            }
        }
    }

    /** Returns null if user cancels (enters "0"/"c"/"cancel"). */
    public static String readLineOrCancel(String label) {
        while (true) {
            String s = readLine(styledPrompt(label + " (0=Cancel): "));
            if (isCancel(s)) return null;
            if (s.length() == 0) {
                println(Ansi.fg("yellow") + "Empty not allowed. Type 0 to cancel." + Ansi.reset());
                continue;
            }
            return s;
        }
    }

    /** Returns Integer.MIN_VALUE if cancel. */
    public static int readIntOrCancel(String label) {
        while (true) {
            String s = readLine(styledPrompt(label + " (0=Cancel): "));
            if (isCancel(s)) return Integer.MIN_VALUE;
            try {
                int v = Integer.parseInt(s);
                if (v < 0) {
                    println(Ansi.fg("yellow") + "Must be non-negative." + Ansi.reset());
                    continue;
                }
                return v;
            } catch (Exception e) {
                println(Ansi.fg("red") + "Enter a valid integer." + Ansi.reset());
            }
        }
    }

    /** Read integer within [min,max]. No cancel path (for menus). */
    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            String s = readLine(styledPrompt(prompt));
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    println(Ansi.fg("yellow") + "Choose between " + min + " and " + max + "." + Ansi.reset());
                    continue;
                }
                return v;
            } catch (Exception e) {
                println(Ansi.fg("red") + "Enter a valid integer." + Ansi.reset());
            }
        }
    }

    /** Returns Double.NaN if cancel. */
    public static double readPositiveDoubleOrCancel(String label) {
        while (true) {
            String s = readLine(styledPrompt(label + " (0=Cancel): "));
            if (isCancel(s)) return Double.NaN;
            try {
                double v = Double.parseDouble(s);
                if (v < 0) {
                    println(Ansi.fg("yellow") + "Must be non-negative." + Ansi.reset());
                    continue;
                }
                return v;
            } catch (Exception e) {
                println(Ansi.fg("red") + "Enter a valid number." + Ansi.reset());
            }
        }
    }

    /** Returns Boolean.TRUE/ FALSE, or null if cancel. */
    public static Boolean readYesNoOrCancel(String label) {
        while (true) {
            String s = readLine(styledPrompt(label + " [y/n] (0=Cancel): ")).toLowerCase();
            if (isCancel(s)) return null;
            if (s.equals("y") || s.equals("yes")) return Boolean.TRUE;
            if (s.equals("n") || s.equals("no")) return Boolean.FALSE;
            println(Ansi.fg("yellow") + "Please answer y/n or 0 to cancel." + Ansi.reset());
        }
    }

    // ------------------ Internals ------------------

    private static String styledPrompt(String s){
        if (!Ansi.isEnabled()) return s;
        return Ansi.fg("cyan") + s + Ansi.reset();
    }

    private static boolean isCancel(String s) {
        if (s == null) return true;
        s = s.trim();
        return s.equals("0") || s.equalsIgnoreCase("cancel") || s.equalsIgnoreCase("c");
    }

    private static String repeat(char c, int n){
        if (n <= 0) return "";
        StringBuilder b = new StringBuilder(n);
        for (int i=0;i<n;i++) b.append(c);
        return b.toString();
    }
    private static String repeat(char c, long n){
        int m = n <= 0 ? 0 : (n > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)n);
        return repeat(c, m);
    }
    private static String repeat(char c, short n){ return repeat(c, (int)n); }
    private static String repeat(char c, byte n){  return repeat(c, (int)n); }

    private static String repeat(char c, Integer n){ return repeat(c, n == null ? 0 : n.intValue()); }
    private static String repeat(char c, int n, int extra){ return repeat(c, n + extra); }

    private static String repeat(char c, String n){
        int m=0;
        try{ m=Integer.parseInt(n);}catch(Exception ignored){}
        return repeat(c, m);
    }

    private static String center(String s, int w){
        if (s == null) s = "";
        int pad = w - s.length();
        if (pad <= 0) return s;
        int left = pad / 2;
        int right = pad - left;
        StringBuilder b = new StringBuilder(w);
        for (int i=0;i<left;i++) b.append(' ');
        b.append(s);
        for (int i=0;i<right;i++) b.append(' ');
        return b.toString();
    }
}
