package atinka.util;

public final class Stopwatch {
    private long startNanos;
    private long elapsed; // when stopped

    private Stopwatch(long startNanos){ this.startNanos = startNanos; }

    public static Stopwatch startNew(){ return new Stopwatch(System.nanoTime()); }

    public long stopNanos(){
        if (startNanos == 0) return elapsed;
        elapsed = System.nanoTime() - startNanos;
        startNanos = 0;
        return elapsed;
    }

    public double millis(){
        long ns = (startNanos == 0) ? elapsed : (System.nanoTime() - startNanos);
        return ns / 1_000_000.0;
    }
}
