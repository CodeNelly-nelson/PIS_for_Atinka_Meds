package atinka.util;

import java.util.concurrent.atomic.AtomicLong;

/** Simple in-memory ID generator for demos. */
public final class IdGen {
    private static final AtomicLong DRUG = new AtomicLong(1000);
    private static final AtomicLong SUP = new AtomicLong(1000);
    private static final AtomicLong CUS = new AtomicLong(1000);
    private static final AtomicLong PUR = new AtomicLong(1);
    private static final AtomicLong SAL = new AtomicLong(1);
    private IdGen() {}
    public static String nextDrug() { return "D" + DRUG.getAndIncrement(); }
    public static String nextSupplier() { return "S" + SUP.getAndIncrement(); }
    public static String nextCustomer() { return "C" + CUS.getAndIncrement(); }
    public static String nextPurchase() { return "P" + String.format("%05d", PUR.getAndIncrement()); }
    public static String nextSale() { return "X" + String.format("%05d", SAL.getAndIncrement()); }
}