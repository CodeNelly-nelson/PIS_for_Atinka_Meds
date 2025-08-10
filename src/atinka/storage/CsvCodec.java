package atinka.storage;

import java.util.ArrayList;
import java.util.List;

/** Tiny CSV (pipe) codec with escaping for '|' and '\\'. */
public final class CsvCodec {
    private CsvCodec() {}

    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append('|');
            sb.append(escape(fields[i] == null ? "" : fields[i]));
        }
        return sb.toString();
    }

    public static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (esc) { cur.append(c); esc = false; }
            else if (c == '\\') { esc = true; }
            else if (c == '|') { out.add(cur.toString()); cur.setLength(0); }
            else { cur.append(c); }
        }
        out.add(cur.toString());
        return out;
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '|' || c == '\\') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }
}