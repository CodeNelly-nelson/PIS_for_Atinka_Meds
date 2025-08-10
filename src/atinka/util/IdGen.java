package atinka.util;

/**
 * Simple, human-friendly IDs with short prefixes.
 * Drug: D001, Supplier: S001, Customer: C001, Purchase: P00001, Sale: R00001
 */
public final class IdGen {
    private static int drugSeq = 0;
    private static int supplierSeq = 0;
    private static int customerSeq = 0;
    private static int purchaseSeq = 0;
    private static int saleSeq = 0;

    private IdGen() {}

    private static synchronized String pad3(int n) {
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        if (n < 10) sb.append("00");
        else if (n < 100) sb.append('0');
        sb.append(s);
        return sb.toString();
    }

    private static synchronized String pad5(int n) {
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < 5; i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }

    public static synchronized String nextDrug()     { drugSeq++;     return "D" + pad3(drugSeq); }
    public static synchronized String nextSupplier() { supplierSeq++; return "S" + pad3(supplierSeq); }
    public static synchronized String nextCustomer() { customerSeq++; return "C" + pad3(customerSeq); }
    public static synchronized String nextPurchase() { purchaseSeq++; return "P" + pad5(purchaseSeq); }
    public static synchronized String nextSale()     { saleSeq++;     return "R" + pad5(saleSeq); }
}
