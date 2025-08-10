package atinka.util;

/** Nano‑precision stopwatch using System.nanoTime(). */
public final class Stopwatch {
    private long start;
    private Stopwatch() { this.start = System.nanoTime(); }
    public static Stopwatch startNew() { return new Stopwatch(); }
    public long stopNanos() { return System.nanoTime() - start; }
}