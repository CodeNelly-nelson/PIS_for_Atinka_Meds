package atinka.util;

public final class Metrics {
    private long comparisons;
    private long nanos;

    public void addComparisons(long c){ comparisons += c; }
    public long getComparisons(){ return comparisons; }

    public void setNanos(long ns){ nanos = ns; }
    public long getNanos(){ return nanos; }
    public double millis(){ return nanos / 1_000_000.0; }
}
