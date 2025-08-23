package atinka.dsa;

/** String helpers without java.util. */
public final class Strings {
    private Strings(){}

    public static int compareIgnoreCase(String a, String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            char ca = toLower(a.charAt(i));
            char cb = toLower(b.charAt(i));
            if (ca != cb) return ca < cb ? -1 : 1;
        }
        if (a.length() == b.length()) return 0;
        return a.length() < b.length() ? -1 : 1;
    }

    /** Case-insensitive substring search; returns start index or -1. */
    public static int indexOfIgnoreCase(String haystack, String needle) {
        if (needle == null) return -1;
        if (needle.length() == 0) return 0;
        int n = haystack.length(), m = needle.length();
        for (int i = 0; i + m <= n; i++) {
            int k = 0;
            while (k < m) {
                char a = toLower(haystack.charAt(i + k));
                char b = toLower(needle.charAt(k));
                if (a != b) break;
                k++;
            }
            if (k == m) return i;
        }
        return -1;
    }

    /** ASCII lower for A–Z; leaves others unchanged. */
    public static char toLower(char c) {
        return (c >= 'A' && c <= 'Z') ? (char)(c + 32) : c;
    }
}
