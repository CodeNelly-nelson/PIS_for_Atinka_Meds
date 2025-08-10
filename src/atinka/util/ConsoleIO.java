package atinka.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Console utilities without java.util.Scanner.
 * Adds cancel-friendly reads: entering "0" returns null (cancel).
 */
public final class ConsoleIO {
    private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));

    private ConsoleIO() {}

    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name");
            if (os != null && os.toLowerCase().startsWith("windows")) {
                for (int i = 0; i < 60; i++) System.out.println();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {}
    }

    public static void printHeader(String title) {
        println("========================================");
        println(title);
        println("========================================");
    }

    public static void println(String s) { System.out.println(s); }
    public static void print(String s) { System.out.print(s); }

    /** Reads a whole line (can be empty). */
    public static String readLine(String prompt) {
        if (prompt != null) print(prompt);
        try {
            String line = READER.readLine();
            return line == null ? "" : line.trim();
        } catch (IOException e) {
            return "";
        }
    }

    /** Like readLine, but if user enters "0" returns null (treat as cancel). */
    public static String readLineOrCancel(String prompt) {
        String s = readLine(prompt + " (0 to cancel): ");
        if ("0".equals(s)) return null;
        return s;
    }

    /** Reads an integer in [min, max]. Re-prompts on invalid input. */
    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    println("Enter a number between " + min + " and " + max + ".");
                } else {
                    return v;
                }
            } catch (Exception ex) {
                println("Invalid number. Try again.");
            }
        }
    }

    /** Reads an integer, but "0" cancels -> returns Integer.MIN_VALUE. */
    public static int readIntOrCancel(String prompt) {
        String s = readLine(prompt + " (0 to cancel): ");
        if ("0".equals(s)) return Integer.MIN_VALUE;
        try { return Integer.parseInt(s); } catch (Exception ex) { return Integer.MIN_VALUE + 1; }
    }

    /** Reads a strictly positive double (> 0). */
    public static double readPositiveDouble(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                double v = Double.parseDouble(s);
                if (v <= 0) println("Enter a value > 0.");
                else return v;
            } catch (Exception ex) {
                println("Invalid number. Try again.");
            }
        }
    }

    /** Reads a positive double, but "0" cancels -> returns Double.NEGATIVE_INFINITY. */
    public static double readPositiveDoubleOrCancel(String prompt) {
        String s = readLine(prompt + " (0 to cancel): ");
        if ("0".equals(s)) return Double.NEGATIVE_INFINITY;
        try {
            double v = Double.parseDouble(s);
            return v > 0 ? v : Double.NEGATIVE_INFINITY + 1.0;
        } catch (Exception ex) {
            return Double.NEGATIVE_INFINITY + 2.0;
        }
    }

    /** Yes/No prompt. Returns true for 'y', false for 'n'. Null = cancel if user enters '0'. */
    public static Boolean readYesNoOrCancel(String prompt) {
        while (true) {
            String s = readLine(prompt + " [y/n, 0=cancel]: ");
            if (s == null) return null;
            s = s.trim();
            if (s.equals("0")) return null;
            if (s.equalsIgnoreCase("y")) return Boolean.TRUE;
            if (s.equalsIgnoreCase("n")) return Boolean.FALSE;
            println("Please enter 'y', 'n', or 0 to cancel.");
        }
    }
}
