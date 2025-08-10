package atinka.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Console utilities without java.util.Scanner.
 * Provides prompting, typed reads, and simple screen formatting.
 */
public final class ConsoleIO {
    private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));

    private ConsoleIO() {}

    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name");
            if (os != null && os.toLowerCase().startsWith("windows")) {
                // Best-effort: print many newlines
                for (int i = 0; i < 60; i++) System.out.println();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {}
    }

    public static void printHeader(String title) {
        println("==============================");
        println(title);
        println("==============================");
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
}