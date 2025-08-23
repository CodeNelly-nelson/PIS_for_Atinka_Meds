package atinka.util;

/**
 * Simple, human-friendly sequential IDs with short prefixes.
 *
 * Formats (defaults):
 *   Drug     -> D001, D002, ...        (width 3)
 *   Supplier -> S001, S002, ...        (width 3)
 *   Customer -> C001, C002, ...        (width 3)
 *   Purchase -> P00001, P00002, ...    (width 5)
 *   Sale     -> R00001, R00002, ...    (width 5)
 *
 * Features:
 *  - Thread-safe (all public methods synchronized).
 *  - No java.util collections used.
 *  - Safe seeding from a known last number or last ID string.
 *  - "Peek" methods to inspect the next ID without incrementing.
 *  - If a sequence exceeds its width, the ID simply grows (e.g., D1000).
 *
 * Notes:
 *  - Call seed* methods after you load your CSVs if you want continuity.
 *  - This class only manages counters/formatting; it does not scan files.
 */
public final class IdGen {

    // ---- Prefix constants (single char for minimal validation) ----
    public static final char PREF_DRUG     = 'D';
    public static final char PREF_SUPPLIER = 'S';
    public static final char PREF_CUSTOMER = 'C';
    public static final char PREF_PURCHASE = 'P';
    public static final char PREF_SALE     = 'R';

    // ---- Fixed widths for zero-padding ----
    private static final int WIDTH_3 = 3;
    private static final int WIDTH_5 = 5;

    // ---- Internal counters (monotonic per-process) ----
    private static int drugSeq     = 0;
    private static int supplierSeq = 0;
    private static int customerSeq = 0;
    private static int purchaseSeq = 0;
    private static int saleSeq     = 0;

    private IdGen() {}

    // -----------------------------------------------------------------
    // PUBLIC API — NEXT
    // -----------------------------------------------------------------

    public static synchronized String nextDrug() {
        drugSeq++;
        return build(PREF_DRUG, drugSeq, WIDTH_3);
    }

    public static synchronized String nextSupplier() {
        supplierSeq++;
        return build(PREF_SUPPLIER, supplierSeq, WIDTH_3);
    }

    public static synchronized String nextCustomer() {
        customerSeq++;
        return build(PREF_CUSTOMER, customerSeq, WIDTH_3);
    }

    public static synchronized String nextPurchase() {
        purchaseSeq++;
        return build(PREF_PURCHASE, purchaseSeq, WIDTH_5);
    }

    public static synchronized String nextSale() {
        saleSeq++;
        return build(PREF_SALE, saleSeq, WIDTH_5);
    }

    // -----------------------------------------------------------------
    // PUBLIC API — PEEK (does not increment)
    // -----------------------------------------------------------------

    public static synchronized String peekNextDrug() {
        return build(PREF_DRUG, drugSeq + 1, WIDTH_3);
    }

    public static synchronized String peekNextSupplier() {
        return build(PREF_SUPPLIER, supplierSeq + 1, WIDTH_3);
    }

    public static synchronized String peekNextCustomer() {
        return build(PREF_CUSTOMER, customerSeq + 1, WIDTH_3);
    }

    public static synchronized String peekNextPurchase() {
        return build(PREF_PURCHASE, purchaseSeq + 1, WIDTH_5);
    }

    public static synchronized String peekNextSale() {
        return build(PREF_SALE, saleSeq + 1, WIDTH_5);
    }

    // -----------------------------------------------------------------
    // PUBLIC API — SEED BY LAST NUMBER
    // Call these after loading existing data to continue sequences.
    // -----------------------------------------------------------------

    public static synchronized void seedDrug(int lastNumber) {
        if (lastNumber > drugSeq) drugSeq = lastNumber;
    }
    public static synchronized void seedSupplier(int lastNumber) {
        if (lastNumber > supplierSeq) supplierSeq = lastNumber;
    }
    public static synchronized void seedCustomer(int lastNumber) {
        if (lastNumber > customerSeq) customerSeq = lastNumber;
    }
    public static synchronized void seedPurchase(int lastNumber) {
        if (lastNumber > purchaseSeq) purchaseSeq = lastNumber;
    }
    public static synchronized void seedSale(int lastNumber) {
        if (lastNumber > saleSeq) saleSeq = lastNumber;
    }

    // -----------------------------------------------------------------
    // PUBLIC API — SEED BY LAST ID STRING (e.g., "D037", "P00012")
    // Returns true if the string parsed and seeded successfully.
    // -----------------------------------------------------------------

    public static synchronized boolean seedDrugFromId(String lastId) {
        int n = parseSuffixIfMatches(lastId, PREF_DRUG);
        if (n <= 0) return false;
        if (n > drugSeq) drugSeq = n;
        return true;
    }

    public static synchronized boolean seedSupplierFromId(String lastId) {
        int n = parseSuffixIfMatches(lastId, PREF_SUPPLIER);
        if (n <= 0) return false;
        if (n > supplierSeq) supplierSeq = n;
        return true;
    }

    public static synchronized boolean seedCustomerFromId(String lastId) {
        int n = parseSuffixIfMatches(lastId, PREF_CUSTOMER);
        if (n <= 0) return false;
        if (n > customerSeq) customerSeq = n;
        return true;
    }

    public static synchronized boolean seedPurchaseFromId(String lastId) {
        int n = parseSuffixIfMatches(lastId, PREF_PURCHASE);
        if (n <= 0) return false;
        if (n > purchaseSeq) purchaseSeq = n;
        return true;
    }

    public static synchronized boolean seedSaleFromId(String lastId) {
        int n = parseSuffixIfMatches(lastId, PREF_SALE);
        if (n <= 0) return false;
        if (n > saleSeq) saleSeq = n;
        return true;
    }

    // -----------------------------------------------------------------
    // OPTIONAL — RESET (mainly for tests/tools)
    // -----------------------------------------------------------------

    public static synchronized void resetAll() {
        drugSeq = supplierSeq = customerSeq = purchaseSeq = saleSeq = 0;
    }

    // -----------------------------------------------------------------
    // INTERNAL HELPERS (no java.util)
    // -----------------------------------------------------------------

    /** Builds PREFIX + zero-padded numeric body with minimum width. */
    private static String build(char prefix, int n, int minWidth) {
        if (n < 0) n = 0;
        String body = pad(n, minWidth);
        StringBuilder sb = new StringBuilder(1 + body.length());
        sb.append(prefix).append(body);
        return sb.toString();
    }

    /** Returns numeric suffix if the string starts with expected prefix; otherwise -1. */
    private static int parseSuffixIfMatches(String id, char expectedPrefix) {
        if (id == null) return -1;
        id = id.trim();
        if (id.length() < 2) return -1;
        if (id.charAt(0) != expectedPrefix) return -1;
        // parse all remaining chars as digits
        int val = 0;
        for (int i = 1; i < id.length(); i++) {
            char c = id.charAt(i);
            if (c < '0' || c > '9') return -1;
            int d = c - '0';
            // val = val * 10 + d, with basic overflow guard
            int nv = (val * 10) + d;
            if (nv < val) return -1; // overflow (extremely unlikely with normal IDs)
            val = nv;
        }
        return (val <= 0) ? -1 : val;
    }

    /** Minimal zero-padding without allocations beyond the builder itself. */
    private static String pad(int n, int width) {
        String s = String.valueOf(n);
        int len = s.length();
        if (len >= width) return s;
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < width - len; i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }
}
