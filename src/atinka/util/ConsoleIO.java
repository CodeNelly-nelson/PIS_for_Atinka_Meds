package atinka.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * ConsoleIO: centralizes robust terminal I/O for the CLI.
 * - No java.util collections; uses standard I/O only.
 * - "Cancel anywhere": any prompt accepts:
 *     - "0", "c", or "cancel" (any case) => cancel sentinel
 */
public final class ConsoleIO {
    private static final BufferedReader IN =
            new BufferedReader(new InputStreamReader(System.in));

    private ConsoleIO() {}

    // ------------------ Printing helpers ------------------

    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                for (int i = 0; i < 80; i++) System.out.println();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {}
    }

    public static void printHeader(String title) {
        println("==================================================");
        println(" " + title);
        println("==================================================");
    }

    public static void println(String s) { System.out.println(s); }
    public static void print(String s) { System.out.print(s); }

    // ------------------ Read helpers ------------------

    /** Raw line (can return empty). */
    public static String readLine(String prompt) {
        while (true) {
            try {
                if (prompt != null) print(prompt);
                String s = IN.readLine();
                if (s == null) return ""; // EOF => empty
                return s.trim();
            } catch (Exception e) {
                println("Input error, try again.");
            }
        }
    }

    /** Returns null if user cancels (enters "0"/"c"/"cancel"). */
    public static String readLineOrCancel(String label) {
        while (true) {
            String s = readLine(label + " (0=Cancel): ");
            if (isCancel(s)) return null;
            if (s.length() == 0) {
                println("Empty not allowed. Type 0 to cancel.");
                continue;
            }
            return s;
        }
    }

    /** Returns Integer.MIN_VALUE if cancel. */
    public static int readIntOrCancel(String label) {
        while (true) {
            String s = readLine(label + " (0=Cancel): ");
            if (isCancel(s)) return Integer.MIN_VALUE;
            try {
                int v = Integer.parseInt(s);
                if (v < 0) {
                    println("Must be non-negative.");
                    continue;
                }
                return v;
            } catch (Exception e) {
                println("Enter a valid integer.");
            }
        }
    }

    /** Read integer within [min,max]. No cancel path (for menus). */
    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    println("Choose between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (Exception e) {
                println("Enter a valid integer.");
            }
        }
    }

    /** Returns Double.NaN if cancel. */
    public static double readPositiveDoubleOrCancel(String label) {
        while (true) {
            String s = readLine(label + " (0=Cancel): ");
            if (isCancel(s)) return Double.NaN;
            try {
                double v = Double.parseDouble(s);
                if (v < 0) {
                    println("Must be non-negative.");
                    continue;
                }
                return v;
            } catch (Exception e) {
                println("Enter a valid number.");
            }
        }
    }

    /** Returns Boolean.TRUE/ FALSE, or null if cancel. */
    public static Boolean readYesNoOrCancel(String label) {
        while (true) {
            String s = readLine(label + " [y/n] (0=Cancel): ").toLowerCase();
            if (isCancel(s)) return null;
            if (s.equals("y") || s.equals("yes")) return Boolean.TRUE;
            if (s.equals("n") || s.equals("no")) return Boolean.FALSE;
            println("Please answer y/n or 0 to cancel.");
        }
    }

    private static boolean isCancel(String s) {
        if (s == null) return true;
        s = s.trim();
        return s.equals("0") || s.equalsIgnoreCase("cancel") || s.equalsIgnoreCase("c");
    }
}
