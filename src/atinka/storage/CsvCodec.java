package atinka.storage;

/**
 * Minimal CSV (pipe) codec without java.util.
 * Escapes '|' and '\\' using a leading backslash.
 */
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

    public static String[] split(String line) {
        String[] parts = new String[8];
        int count = 0;
        StringBuilder cur = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (esc) { cur.append(c); esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '|') {
                if (count == parts.length) parts = grow(parts);
                parts[count++] = cur.toString();
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (count == parts.length) parts = grow(parts);
        parts[count++] = cur.toString();
        String[] out = new String[count];
        for (int i = 0; i < count; i++) out[i] = parts[i];
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

    private static String[] grow(String[] a) {
        String[] b = new String[a.length << 1];
        for (int i = 0; i < a.length; i++) b[i] = a[i];
        return b;
    }
}