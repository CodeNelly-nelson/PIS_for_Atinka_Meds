package atinka.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/** Console helpers with cancel-anywhere and friendlier prompts. */
public final class ConsoleIO {
    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    private ConsoleIO() {}

    // Basic output
    public static void println(String s){ System.out.println(s == null ? "" : s); }
    public static void print(String s){ System.out.print(s == null ? "" : s); }

    // Screen mgmt
    public static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    public static void printHeader(String title) {
        Tui.banner("Atinka Meds — " + title, "Offline-first • Custom DSA • No external libs");
    }

    // Reading with cancel support (ENTER=cancel for strings, 0=cancel for numbers)
    public static String readLine(String prompt) {
        try {
            System.out.print(Ansi.color("? ", Ansi.FG_CYAN) + prompt + ": ");
            return IN.readLine();
        } catch (Exception e) { return ""; }
    }

    public static String readLineOrCancel(String label) {
        String s = readLine(label + " (ENTER=cancel)");
        if (s == null || s.trim().length() == 0) return null;
        return s.trim();
    }

    public static int readIntInRange(String label, int min, int max) {
        while (true) {
            String s = readLine(label + " ["+min+"-"+max+"]  (0=cancel)");
            if (s == null) return min;
            s = s.trim();
            if (s.length() == 0 || s.equals("0")) return 0;
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    Tui.toastWarn("Choose between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (Exception e) {
                Tui.toastError("Invalid number.");
            }
        }
    }

    public static int readIntOrCancel(String label) {
        while (true) {
            String s = readLine(label + " (0=cancel)");
            if (s == null) return Integer.MIN_VALUE;
            s = s.trim();
            if (s.length() == 0 || s.equals("0")) return Integer.MIN_VALUE;
            try { return Integer.parseInt(s); }
            catch (Exception e) { Tui.toastError("Invalid number."); }
        }
    }

    public static double readPositiveDoubleOrCancel(String label) {
        while (true) {
            String s = readLine(label + " (ENTER=cancel)");
            if (s == null || s.trim().length() == 0) return Double.NEGATIVE_INFINITY;
            try {
                double v = Double.parseDouble(s.trim());
                if (v < 0) { Tui.toastWarn("Must be >= 0."); continue; }
                return v;
            } catch (Exception e) { Tui.toastError("Invalid number."); }
        }
    }

    public static Boolean readYesNoOrCancel(String label) {
        while (true) {
            String s = readLine(label + " [y/n] (ENTER=cancel)");
            if (s == null || s.trim().length()==0) return null;
            s = s.trim().toLowerCase();
            if (s.equals("y") || s.equals("yes")) return Boolean.TRUE;
            if (s.equals("n") || s.equals("no")) return Boolean.FALSE;
            Tui.toastWarn("Please answer y/n.");
        }
    }
}
