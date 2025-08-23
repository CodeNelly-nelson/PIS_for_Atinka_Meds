package atinka.report;

import atinka.dsa.BinarySearch;
import atinka.dsa.Comparator;
import atinka.dsa.InsertionSort;
import atinka.dsa.MergeSort;
import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.service.DrugService;
import atinka.storage.ReportsFS;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Generates an algorithm performance report using ONLY custom DS/algorithms.
 * Output goes to data/reports/performance.txt (single file, overwritten).
 */
public final class PerformanceReport {
    private PerformanceReport(){}

    public static java.nio.file.Path generate(DrugService drugsSvc){
        StringBuilder out = new StringBuilder();

        // ---------- data snapshot ----------
        Vec<Drug> data = drugsSvc.all(); // copy
        out.append("Atinka Meds — Algorithm Performance Report\n");
        out.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        out.append("Data snapshot\n");
        out.append("- Drug count: ").append(data.size()).append("\n\n");

        // ---------- Sorting (by name) ----------
        out.append("Sorting (by name)\n");
        if (data.size() == 0) {
            out.append("No data to sort.\n\n");
        } else {
            Comparator<Drug> byName = (a,b) -> compareIgnoreCase(a.getName(), b.getName());

            // InsertionSort
            Vec<Drug> v1 = copyOf(data);
            shuffle(v1);
            long t1 = now();
            long insComparisons = sortAndCountComparisonsInsertion(v1, byName);
            long dt1 = now() - t1;

            // MergeSort
            Vec<Drug> v2 = copyOf(data);
            shuffle(v2);
            long t2 = now();
            long merComparisons = sortAndCountComparisonsMerge(v2, byName);
            long dt2 = now() - t2;

            out.append("- InsertionSort: comparisons=").append(insComparisons)
                    .append(", time=").append(toMillis(dt1)).append(" ms\n");
            out.append("- MergeSort:     comparisons=").append(merComparisons)
                    .append(", time=").append(toMillis(dt2)).append(" ms\n\n");

            out.append("Theoretical\n");
            out.append("- InsertionSort:  O(n^2) worst, Ω(n) best (nearly-sorted).\n");
            out.append("- MergeSort:      O(n log n) time, Ω(n log n); stable; O(n) extra space.\n\n");
        }

        // ---------- Searching (binary search vs hashmap) ----------
        out.append("Searching (by code)\n");
        if (data.size() == 0){
            out.append("No data to search.\n\n");
        } else {
            Vec<Drug> byCode = copyOf(data);
            Comparator<Drug> cmpCode = (a,b) -> compareIgnoreCase(a.getCode(), b.getCode());
            MergeSort.sort(byCode, cmpCode);

            // Build sample keys: up to 10 + a missing one
            String[] keys = sampleKeys(byCode);

            // Binary search comparisons (count via comparator wrapper)
            CountingComparator<Drug> countCmp = new CountingComparator<>(cmpCode);
            for (int i=0;i<keys.length;i++){
                Drug probe = new Drug(keys[i], "_", 0.0, 0, LocalDate.now(), 0);
                BinarySearch.indexOf(byCode, probe, countCmp);
            }
            long binComparisons = countCmp.count;

            // Hash map lookups timing (average O(1))
            long t0 = now();
            for (int i=0;i<keys.length;i++){ drugsSvc.indexByCode().get(keys[i]); }
            long dt = now() - t0;

            out.append("- BinarySearch (on code): comparisons≈")
                    .append(binComparisons).append(" over ").append(keys.length).append(" lookups\n");
            out.append("- HashMap lookup:        time=")
                    .append(toMillis(dt)).append(" ms over ").append(keys.length).append(" lookups\n\n");

            out.append("Theoretical\n");
            out.append("- Binary search: O(log n), Ω(1) best; requires sorted data.\n");
            out.append("- Hash map:      O(1) average, O(n) worst with collisions.\n\n");
        }

        // ---------- Linear scan: name contains ----------
        out.append("Searching (name contains)\n");
        if (data.size() == 0){
            out.append("No data to search.\n\n");
        } else {
            String term = data.get(0).getName();
            term = (term == null) ? "" : term;
            term = term.length() > 3 ? term.substring(0,3).toLowerCase() : term.toLowerCase();

            int visited = 0, hits = 0;
            long t = now();
            for (int i=0;i<data.size();i++){
                visited++;
                Drug d = data.get(i);
                if (indexOfIgnoreCase(d.getName(), term) >= 0) hits++;
            }
            long dt = now() - t;
            out.append("- Linear scan: elements_visited=").append(visited)
                    .append(", hits=").append(hits)
                    .append(", time=").append(toMillis(dt)).append(" ms\n\n");

            out.append("Theoretical\n");
            out.append("- Linear search: O(n) worst/avg, Ω(1) best if first item matches.\n\n");
        }

        // ---------- Qualitative DS notes ----------
        out.append("Other structures (qualitative complexity)\n");
        out.append("- LinkedQueue enqueue/dequeue: O(1).\n");
        out.append("- LinkedStack push/pop:        O(1).\n");
        out.append("- MinHeap insert/extractMin:   O(log n), peek O(1).\n\n");

        out.append("Notes\n");
        out.append("- Times depend on dataset size and machine.\n");
        out.append("- Comparisons counted via a wrapper around custom Comparator.\n");

        return ReportsFS.writeReport("performance.txt", out.toString());
    }

    // -------- helpers (no java.util) --------
    private static Vec<Drug> copyOf(Vec<Drug> v){
        Vec<Drug> c = new Vec<>(v.size());
        for (int i=0;i<v.size();i++) c.add(v.get(i));
        return c;
    }

    /** Fisher–Yates shuffle in-place on Vec using Math.random(). */
    private static void shuffle(Vec<Drug> v){
        for (int i=v.size()-1;i>0;i--){
            int j = (int)Math.floor(Math.random()*(i+1));
            Drug tmp = v.get(i);
            v.set(i, v.get(j));
            v.set(j, tmp);
        }
    }

    private static String[] sampleKeys(Vec<Drug> sortedByCode){
        int n = sortedByCode.size();
        int take = n < 10 ? n : 10;
        String[] keys = new String[take + 1];
        for (int i=0;i<take;i++) keys[i] = sortedByCode.get(i).getCode();
        keys[take] = "__MISSING__";
        return keys;
    }

    private static long sortAndCountComparisonsInsertion(Vec<Drug> v, Comparator<Drug> base){
        CountingComparator<Drug> c = new CountingComparator<>(base);
        InsertionSort.sort(v, c);
        return c.count;
    }
    private static long sortAndCountComparisonsMerge(Vec<Drug> v, Comparator<Drug> base){
        CountingComparator<Drug> c = new CountingComparator<>(base);
        MergeSort.sort(v, c);
        return c.count;
    }

    private static int compareIgnoreCase(String a, String b){
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        int na=a.length(), nb=b.length();
        int n = na < nb ? na : nb;
        for (int i=0;i<n;i++){
            char ca = toLower(a.charAt(i));
            char cb = toLower(b.charAt(i));
            if (ca != cb) return (ca < cb) ? -1 : 1;
        }
        if (na == nb) return 0;
        return (na < nb) ? -1 : 1;
    }
    private static char toLower(char c){ return (c>='A'&&c<='Z')?(char)(c+32):c; }

    /** naive case-insensitive substring match; returns index or -1 */
    private static int indexOfIgnoreCase(String hay, String needleLower){
        if (hay == null || needleLower == null) return -1;
        int n = hay.length(), m = needleLower.length();
        if (m == 0) return 0;
        for (int i=0;i+m<=n;i++){
            int k=0; while(k<m){
                char a = toLower(hay.charAt(i+k));
                char b = needleLower.charAt(k);
                if (a != b) break; k++;
            }
            if (k==m) return i;
        }
        return -1;
    }

    private static final class CountingComparator<T> implements Comparator<T> {
        private final Comparator<T> base;
        long count;
        CountingComparator(Comparator<T> b){ this.base=b; }
        @Override public int compare(T a, T b){ count++; return base.compare(a,b); }
    }

    private static long now(){ return System.nanoTime(); }
    private static String toMillis(long nanos){
        double ms = ((double)nanos)/1_000_000.0;
        long m = Math.round(ms * 1000.0); // 3dp
        String sign = m<0?"-":"";
        if (m<0) m=-m;
        long i = m/1000; long f = m%1000;
        StringBuilder sb=new StringBuilder();
        sb.append(sign).append(i).append('.');
        if (f<100) sb.append('0');
        if (f<10) sb.append('0');
        sb.append(f);
        return sb.toString();
    }
}
