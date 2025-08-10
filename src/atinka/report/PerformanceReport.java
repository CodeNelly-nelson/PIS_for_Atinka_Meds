package atinka.report;

import atinka.dsa.BinarySearch;
import atinka.dsa.Comparator;
import atinka.dsa.InsertionSort;
import atinka.dsa.MergeSort;
import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.service.DrugService;
import atinka.storage.ReportsFS;
import atinka.util.Metrics;
import atinka.util.Stopwatch;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Algorithm performance report (custom DS only).
 * Writes a single file: data/reports/performance.txt
 */
public final class PerformanceReport {
    private PerformanceReport() {}

    public static Path generate(DrugService drugs) {
        StringBuilder out = new StringBuilder();
        Vec<Drug> data = drugs.all(); // custom Vec copy

        out.append("Atinka Meds — Algorithm Performance Report\n");
        out.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        out.append("Data snapshot\n");
        out.append("- Drug count: ").append(data.size()).append("\n\n");

        // ---------------- Sorting (by NAME) ----------------
        out.append("Sorting (by name)\n");
        if (data.size() == 0) {
            out.append("No data to sort.\n\n");
        } else {
            Comparator<Drug> byName = (a,b) -> a.getName().compareToIgnoreCase(b.getName());

            Vec<Drug> copy1 = copyOf(data);
            shuffleVec(copy1);
            Metrics mIns = new Metrics();
            Stopwatch sw1 = Stopwatch.startNew();
            InsertionSort.sort(copy1, new CountingComparator<>(byName, mIns));
            mIns.setNanos(sw1.stopNanos());

            Vec<Drug> copy2 = copyOf(data);
            shuffleVec(copy2);
            Metrics mMer = new Metrics();
            Stopwatch sw2 = Stopwatch.startNew();
            MergeSort.sort(copy2, new CountingComparator<>(byName, mMer));
            mMer.setNanos(sw2.stopNanos());

            out.append(line2("- InsertionSort: comparisons=%d, time=%.3f ms\n", mIns.getComparisons(), mIns.millis()));
            out.append(line2("- MergeSort:     comparisons=%d, time=%.3f ms\n\n", mMer.getComparisons(), mMer.millis()));

            out.append("Theoretical\n");
            out.append("- InsertionSort:  O(n^2) worst, Ω(n) best (nearly-sorted).\n");
            out.append("- MergeSort:      O(n log n) time, Ω(n log n); stable; O(n) extra space.\n\n");
        }

        // --------------- Searching by CODE -----------------
        out.append("Searching (by code)\n");
        if (data.size() == 0) {
            out.append("No data to search.\n\n");
        } else {
            Vec<Drug> sortedByCode = copyOf(data);
            Comparator<Drug> byCode = (a,b) -> a.getCode().compareToIgnoreCase(b.getCode());
            MergeSort.sort(sortedByCode, byCode);

            String[] keys = sampleKeys(sortedByCode);

            Metrics mBin = new Metrics();
            Comparator<Drug> countingByCode = new CountingComparator<>(byCode, mBin);
            for (int i = 0; i < keys.length; i++) {
                Drug probe = keyProbe(keys[i]);
                BinarySearch.indexOf(sortedByCode, probe, countingByCode);
            }

            Metrics mHM = new Metrics();
            Stopwatch swHM = Stopwatch.startNew();
            for (int i = 0; i < keys.length; i++) { drugs.indexByCode().get(keys[i]); }
            mHM.setNanos(swHM.stopNanos());

            out.append(line2("- BinarySearch (on code): comparisons≈%d over %d lookups\n", mBin.getComparisons(), keys.length));
            out.append(line2("- HashMap lookup:         time=%.3f ms over %d lookups\n\n", mHM.millis(), keys.length));

            out.append("Theoretical\n");
            out.append("- Binary search: O(log n), Ω(1) best; requires sorted data.\n");
            out.append("- Hash map:      O(1) average, O(n) worst with collisions.\n\n");
        }

        // --------------- Name contains (linear scan) ---------------
        out.append("Searching (name contains)\n");
        if (data.size() == 0) {
            out.append("No data to search.\n\n");
        } else {
            String term = data.get(0).getName();
            term = term.length() > 3 ? term.substring(0,3).toLowerCase() : term.toLowerCase();
            Metrics mLin = new Metrics();
            Stopwatch sw = Stopwatch.startNew();
            int hits = 0;
            for (int i = 0; i < data.size(); i++) {
                mLin.addComparisons(1); // visited one element
                Drug d = data.get(i);
                if (indexOfIgnoreCase(d.getName(), term) >= 0) hits++;
            }
            mLin.setNanos(sw.stopNanos());
            out.append(line3("- Linear scan: elements_visited=%d, hits=%d, time=%.3f ms\n\n",
                    mLin.getComparisons(), hits, mLin.millis()));

            out.append("Theoretical\n");
            out.append("- Linear search: O(n) worst/avg, Ω(1) best if first item matches.\n\n");
        }

        // --------------- DS notes ---------------
        out.append("Other structures (qualitative complexity)\n");
        out.append("- LinkedQueue enqueue/dequeue: O(1).\n");
        out.append("- LinkedStack push/pop:        O(1).\n");
        out.append("- MinHeap insert/extractMin:   O(log n), peek O(1).\n\n");

        out.append("Notes\n");
        out.append("- Times depend on dataset size and machine.\n");
        out.append("- Comparisons counted via wrappers around custom Comparator.\n");

        return ReportsFS.writePerformance(out.toString());
    }

    // ---------------- Helpers ----------------

    private static Vec<Drug> copyOf(Vec<Drug> v) {
        Vec<Drug> c = new Vec<>(v.size());
        for (int i = 0; i < v.size(); i++) c.add(v.get(i));
        return c;
    }

    /** Fisher–Yates shuffle using Math.random(), in-place on Vec. */
    private static <T> void shuffleVec(Vec<T> v) {
        for (int i = v.size() - 1; i > 0; i--) {
            int j = (int)Math.floor(Math.random() * (i + 1));
            T tmp = v.get(i);
            v.set(i, v.get(j));
            v.set(j, tmp);
        }
    }

    private static String[] sampleKeys(Vec<Drug> sorted) {
        int n = sorted.size();
        int take = n < 10 ? n : 10;
        String[] keys = new String[take + 1]; // +1 missing
        for (int i = 0; i < take; i++) keys[i] = sorted.get(i).getCode();
        keys[take] = "__MISSING__";
        return keys;
    }

    private static Drug keyProbe(String code) {
        return new Drug(code, "_", 0.0, 0, LocalDate.now(), 0);
    }

    /** Case-insensitive substring search without java.util. */
    private static int indexOfIgnoreCase(String haystack, String needle) {
        if (needle == null || needle.length() == 0) return 0;
        if (haystack == null) return -1;
        int n = haystack.length(), m = needle.length();
        for (int i = 0; i + m <= n; i++) {
            int k = 0; while (k < m) {
                char a = toLower(haystack.charAt(i + k));
                char b = toLower(needle.charAt(k));
                if (a != b) break; k++;
            }
            if (k == m) return i;
        }
        return -1;
    }
    private static char toLower(char c) { return (c >= 'A' && c <= 'Z') ? (char)(c + 32) : c; }

    private static final class CountingComparator<T> implements Comparator<T> {
        private final Comparator<T> base; private final Metrics metrics;
        CountingComparator(Comparator<T> base, Metrics m){ this.base=base; this.metrics=m; }
        @Override public int compare(T a, T b){ metrics.addComparisons(1); return base.compare(a,b); }
    }

    private static String line2(String fmt, Object a, Object b) {
        String s = fmt;
        if (s.contains("%d")) s = s.replaceFirst("%d", String.valueOf(a));
        if (s.contains("%.3f")) s = s.replaceFirst("%\\.3f", toFixed3(b));
        else if (s.contains("%d")) s = s.replaceFirst("%d", String.valueOf(b));
        return s;
    }
    private static String line3(String fmt, Object a, Object b, Object c) {
        String s = fmt;
        if (s.contains("%d")) s = s.replaceFirst("%d", String.valueOf(a));
        if (s.contains("%d")) s = s.replaceFirst("%d", String.valueOf(b));
        if (s.contains("%.3f")) s = s.replaceFirst("%\\.3f", toFixed3(c));
        else if (s.contains("%d")) s = s.replaceFirst("%d", String.valueOf(c));
        return s;
    }
    private static String toFixed3(Object v) {
        double x = 0.0; try { x = (v instanceof Number) ? ((Number)v).doubleValue() : Double.parseDouble(String.valueOf(v)); } catch (Exception ignored) {}
        long m = Math.round(x * 1000.0); String sign = m < 0 ? "-" : ""; if (m < 0) m = -m; long i = m/1000; long f = m%1000;
        StringBuilder sb = new StringBuilder(); sb.append(sign).append(i).append('.'); if (f<100) sb.append('0'); if (f<10) sb.append('0'); sb.append(f); return sb.toString();
    }
}
