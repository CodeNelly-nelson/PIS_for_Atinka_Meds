package atinka.util;

/** ANSI color/style helpers (auto-disables when unsupported). */
public final class Ansi {
    private static final boolean ENABLED = detect();

    private Ansi(){}

    public static String reset(){ return ENABLED ? "\u001B[0m" : ""; }
    public static String bold(){ return ENABLED ? "\u001B[1m" : ""; }
    public static String dim(){ return ENABLED ? "\u001B[2m" : ""; }

    // Foreground
    public static String fg(String name){
        if (!ENABLED) return "";
        if ("black".equals(name))   return "\u001B[30m";
        if ("red".equals(name))     return "\u001B[31m";
        if ("green".equals(name))   return "\u001B[32m";
        if ("yellow".equals(name))  return "\u001B[33m";
        if ("blue".equals(name))    return "\u001B[34m";
        if ("magenta".equals(name)) return "\u001B[35m";
        if ("cyan".equals(name))    return "\u001B[36m";
        if ("white".equals(name))   return "\u001B[37m";
        return "";
    }

    // Background (used for headers)
    public static String bg(String name){
        if (!ENABLED) return "";
        if ("black".equals(name))   return "\u001B[40m";
        if ("red".equals(name))     return "\u001B[41m";
        if ("green".equals(name))   return "\u001B[42m";
        if ("yellow".equals(name))  return "\u001B[43m";
        if ("blue".equals(name))    return "\u001B[44m";
        if ("magenta".equals(name)) return "\u001B[45m";
        if ("cyan".equals(name))    return "\u001B[46m";
        if ("white".equals(name))   return "\u001B[47m";
        return "";
    }

    public static boolean isEnabled(){ return ENABLED; }

    private static boolean detect(){
        try {
            String os = System.getProperty("os.name","").toLowerCase();
            String term = System.getenv("TERM");
            if (os.contains("win")) {
                // Windows terminals often support ANSI now; allow unless explicitly disabled.
                String no = System.getenv("NO_COLOR");
                return no == null || no.length() == 0;
            }
            if (term == null) term = "";
            // If TERM is dumb or explicitly disabled, turn off.
            if ("dumb".equals(term)) return false;
            String no = System.getenv("NO_COLOR");
            return no == null || no.length() == 0;
        } catch (Exception e){
            return false;
        }
    }
}
