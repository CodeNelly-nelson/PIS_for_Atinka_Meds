package atinka.ui;

/** Tiny padding/format helpers for CLI tables (no java.util). */
public final class TextPad {
    private TextPad(){}

    public static String padRight(String s, int w){
        if (s == null) s = "";
        int n = w - s.length();
        if (n <= 0) return s;
        StringBuilder b = new StringBuilder(w);
        b.append(s);
        for (int i=0;i<n;i++) b.append(' ');
        return b.toString();
    }

    public static String padLeft(String s, int w){
        if (s == null) s = "";
        int n = w - s.length();
        if (n <= 0) return s;
        StringBuilder b = new StringBuilder(w);
        for (int i=0;i<n;i++) b.append(' ');
        b.append(s);
        return b.toString();
    }

    /** Fixed 2dp (no locale). */
    public static String toFixed2(double x){
        long m = Math.round(x * 100.0);
        String sign = m < 0 ? "-" : "";
        if (m < 0) m = -m;
        long i = m / 100;
        long f = m % 100;
        StringBuilder sb = new StringBuilder();
        sb.append(sign).append(i).append('.');
        if (f < 10) sb.append('0');
        sb.append(f);
        return sb.toString();
    }
}
