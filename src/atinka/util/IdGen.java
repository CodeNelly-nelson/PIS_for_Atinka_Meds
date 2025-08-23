package atinka.util;

/**
 * Simple, human-friendly sequential IDs with short prefixes.
 * Formats:
 *   Drug     -> D001, D002, ...
 *   Supplier -> S001, S002, ...
 *   Customer -> C001, C002, ...
 *   Purchase -> P00001, P00002, ...
 *   Sale     -> R00001, R00002, ...
 *
 * Thread-safe (synchronized). No java.util collections used.
 * If you need timing utilities, use atinka.util.Stopwatch (separate class).
 */
public final class IdGen {
    private static int drugSeq     = 0;
    private static int supplierSeq = 0;
    private static int customerSeq = 0;
    private static int purchaseSeq = 0;
    private static int saleSeq     = 0;

    private IdGen() {}

    // ---------- Public API (next IDs) ----------

    public static synchronized String nextDrug() {
        drugSeq++;
        return "D" + pad3(drugSeq);
    }

    public static synchronized String nextSupplier() {
        supplierSeq++;
        return "S" + pad3(supplierSeq);
    }

    public static synchronized String nextCustomer() {
        customerSeq++;
        return "C" + pad3(customerSeq);
    }

    public static synchronized String nextPurchase() {
        purchaseSeq++;
        return "P" + pad5(purchaseSeq);
    }

    public static synchronized String nextSale() {
        saleSeq++;
        return "R" + pad5(saleSeq);
    }

    // ---------- Optional: seed from existing data ----------
    // Call these right after loading CSVs if you want sequences to continue
    // from the highest number already present in files.

    public static synchronized void seedDrug(int lastNumber)     { if (lastNumber > drugSeq) drugSeq = lastNumber; }
    public static synchronized void seedSupplier(int lastNumber) { if (lastNumber > supplierSeq) supplierSeq = lastNumber; }
    public static synchronized void seedCustomer(int lastNumber) { if (lastNumber > customerSeq) customerSeq = lastNumber; }
    public static synchronized void seedPurchase(int lastNumber) { if (lastNumber > purchaseSeq) purchaseSeq = lastNumber; }
    public static synchronized void seedSale(int lastNumber)     { if (lastNumber > saleSeq) saleSeq = lastNumber; }

    // ---------- Helpers (no java.util) ----------

    private static String pad3(int n) {
        // returns "001".."999" (or more digits if n >= 1000)
        String s = String.valueOf(n);
        int len = s.length();
        if (len >= 3) return s;
        if (len == 2) return "0" + s;
        return "00" + s; // len == 1
    }

    private static String pad5(int n) {
        // returns "00001".."99999" (or more digits if n >= 100000)
        String s = String.valueOf(n);
        int len = s.length();
        if (len >= 5) return s;
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5 - len; i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }
}
