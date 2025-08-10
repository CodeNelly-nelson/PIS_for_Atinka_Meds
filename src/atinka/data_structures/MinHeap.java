package atinka.data_structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Binary min-heap with comparator. Operations:
 * insert O(log n), peek O(1), extractMin O(log n), size/empty O(1).
 */
public class MinHeap<T> {
    private final List<T> a = new ArrayList<>();
    private final Comparator<? super T> cmp;

    public MinHeap(Comparator<? super T> cmp) { this.cmp = cmp; }

    public void insert(T v) {
        a.add(v);
        siftUp(a.size() - 1);
    }

    public T peek() { return a.isEmpty() ? null : a.get(0); }

    public T extractMin() {
        if (a.isEmpty()) return null;
        T min = a.get(0);
        T last = a.remove(a.size() - 1);
        if (!a.isEmpty()) { a.set(0, last); siftDown(0); }
        return min;
    }

    public int size() { return a.size(); }
    public boolean isEmpty() { return a.isEmpty(); }
    public void clear() { a.clear(); }

    private void siftUp(int i) {
        while (i > 0) {
            int p = (i - 1) / 2;
            if (cmp.compare(a.get(i), a.get(p)) >= 0) break;
            swap(i, p); i = p;
        }
    }

    private void siftDown(int i) {
        int n = a.size();
        while (true) {
            int l = 2 * i + 1, r = l + 1, m = i;
            if (l < n && cmp.compare(a.get(l), a.get(m)) < 0) m = l;
            if (r < n && cmp.compare(a.get(r), a.get(m)) < 0) m = r;
            if (m == i) break;
            swap(i, m); i = m;
        }
    }

    private void swap(int i, int j) {
        T tmp = a.get(i); a.set(i, a.get(j)); a.set(j, tmp);
    }
}