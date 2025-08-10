package atinka.util;

/**
 * Simple metrics holder for comparisons and elapsed time (nanos).
 * No java.util.concurrent; synchronized increment methods.
 */
public final class Metrics {
    private long comparisons;
    private long nanos;

    public synchronized void addComparisons(long c) { comparisons += c; }
    public synchronized long getComparisons() { return comparisons; }

    public synchronized void setNanos(long n) { nanos = n; }
    public synchronized long getNanos() { return nanos; }

    public double millis() { return getNanos() / 1_000_000.0; }
}