package atinka.util;

/**
 * Offline‑first ID generator with no java.util.Random/UUID.
 * Format examples: DRG20250810-00001, SUP20250810-00002, etc.
 * Uses date stamp + monotonic counters. Thread‑safe.
 */
public final class IdGen {
    private static volatile long drugSeq = 0;
    private static volatile long supplierSeq = 0;
    private static volatile long customerSeq = 0;
    private static volatile long purchaseSeq = 0;
    private static volatile long saleSeq = 0;

    private IdGen() {}

    private static String todayStamp() {
        java.time.LocalDate d = java.time.LocalDate.now();
        int y = d.getYear(); int m = d.getMonthValue(); int dd = d.getDayOfMonth();
        StringBuilder sb = new StringBuilder();
        sb.append(y);
        if (m < 10) sb.append('0'); sb.append(m);
        if (dd < 10) sb.append('0'); sb.append(dd);
        return sb.toString();
    }

    private static String pad5(long n) {
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < 5; i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }

    private static synchronized String next(String prefix, long seq, char kind) {
        // `kind` is ignored; kept to avoid overloading clashes; seq is passed by value,
        // we will switch on kind to increment the correct counter atomically.
        if (kind == 'D') drugSeq++; else if (kind == 'S') supplierSeq++; else if (kind == 'C') customerSeq++;
        else if (kind == 'P') purchaseSeq++; else if (kind == 'L') saleSeq++; // 'L' for saLe
        long cur = (kind == 'D') ? drugSeq : (kind == 'S') ? supplierSeq : (kind == 'C') ? customerSeq : (kind == 'P') ? purchaseSeq : saleSeq;
        return prefix + todayStamp() + '-' + pad5(cur);
    }

    public static String nextDrug()     { return next("DRG", drugSeq, 'D'); }
    public static String nextSupplier() { return next("SUP", supplierSeq, 'S'); }
    public static String nextCustomer() { return next("CUS", customerSeq, 'C'); }
    public static String nextPurchase() { return next("PUR", purchaseSeq, 'P'); }
    public static String nextSale()     { return next("SAL", saleSeq, 'L'); }
}