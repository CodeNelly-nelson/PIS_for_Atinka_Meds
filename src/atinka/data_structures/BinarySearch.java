package atinka.data_structures;

import java.util.Comparator;
import java.util.List;

public final class BinarySearch {
    private BinarySearch() {}

    // Returns index of key in sorted list (by cmp), or -1 if not found
    public static <T> int indexOf(List<T> sorted, T key, Comparator<? super T> cmp) {
        int lo = 0, hi = sorted.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int c = cmp.compare(sorted.get(mid), key);
            if (c == 0) return mid;
            if (c < 0) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }
}