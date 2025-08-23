package atinka.dsa;

public final class Comparators {
    private Comparators(){}

    /** Case-insensitive comparator for Strings (nulls last). */
    public static Comparator<String> caseInsensitive() {
        return (a, b) -> {
            if (a == b) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            return Strings.compareIgnoreCase(a, b);
        };
    }

    /** Compare by a String key (optionally ignore case). Nulls last. */
    public static <T> Comparator<T> comparingString(KeyOf<T, String> key, boolean ignoreCase) {
        return (a, b) -> {
            String ka = key.key(a);
            String kb = key.key(b);
            if (ka == kb) return 0;
            if (ka == null) return 1;
            if (kb == null) return -1;
            return ignoreCase ? Strings.compareIgnoreCase(ka, kb) : compare(ka, kb);
        };
    }

    /** Compare by a Double key. Nulls last. */
    public static <T> Comparator<T> comparingDouble(KeyOf<T, Double> key) {
        return (a, b) -> {
            Double ka = key.key(a);
            Double kb = key.key(b);
            if (ka == kb) return 0;
            if (ka == null) return 1;
            if (kb == null) return -1;
            double x = ka - kb;
            if (x < 0) return -1;
            if (x > 0) return 1;
            return 0;
        };
    }

    /** Chain comparators: use 'a', if equal then use 'b'. */
    public static <T> Comparator<T> thenComparing(Comparator<T> a, Comparator<T> b) {
        return (x, y) -> {
            int c = a.compare(x, y);
            return (c != 0) ? c : b.compare(x, y);
        };
    }

    // tiny String compare to avoid java.util
    private static int compare(String a, String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            char ca = a.charAt(i), cb = b.charAt(i);
            if (ca != cb) return ca < cb ? -1 : 1;
        }
        if (a.length() == b.length()) return 0;
        return a.length() < b.length() ? -1 : 1;
    }
}
