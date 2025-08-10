package atinka.util;

import java.util.Scanner;

public final class ConsoleIO {
    private static final Scanner SC = new Scanner(System.in);

    private ConsoleIO() {}

    public static void printHeader(String text) {
        println("==============================");
        println(text);
        println("==============================");
    }

    public static void print(String s) { System.out.print(s); }
    public static void println(String s) { System.out.println(s); }

    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {
            for (int i = 0; i < 4; i++) System.out.println();
        }
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                } else return v;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    public static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            try {
                double d = Double.parseDouble(s);
                if (d < 0) System.out.println("Value must be non-negative.");
                else return d;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid decimal number. Try again.");
            }
        }
    }
}