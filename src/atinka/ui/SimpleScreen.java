package atinka.util;

/**
 * Minimal screen renderer using only ConsoleIO.
 * - Centered header via ConsoleIO.printHeader
 * - Shows an "Algorithms / DS" line (badges)
 * - Then prints any static body lines the caller supplies
 */
public final class SimpleScreen {
    private SimpleScreen(){}

    public static void render(String title, String[] algos, String[] body) {
        if (title == null) title = "";
        if (algos == null) algos = new String[0];
        if (body == null) body = new String[0];

        ConsoleIO.clearScreen();
        ConsoleIO.printHeader(title);

        // algorithms badge line
        String badges = joinBadges(algos);
        if (badges.length() > 0) {
            String tag = Ansi.isEnabled() ? (Ansi.fg("magenta") + "Algorithms / DS: " + Ansi.reset())
                    : "Algorithms / DS: ";
            ConsoleIO.println(tag + badges);
            ConsoleIO.hr();
        }

        // optional static body
        for (int i = 0; i < body.length; i++) ConsoleIO.println(body[i]);
        if (body.length > 0) ConsoleIO.println("");
    }

    private static String joinBadges(String[] a){
        if (a == null || a.length == 0) return "";
        String mag = Ansi.isEnabled()? Ansi.fg("magenta") : "";
        String reset = Ansi.reset();
        StringBuilder b = new StringBuilder();
        for (int i=0;i<a.length;i++){
            if (i > 0) b.append("  ");
            b.append(mag).append("❖").append(reset).append(" ").append(safe(a[i]));
        }
        return b.toString();
    }

    private static String safe(String s){ return s == null ? "" : s; }
}
