package atinka.dsa;

/**
 * Vec<T> — simple dynamic array (no java.util).
 * - Amortized O(1) append
 * - O(1) get/set
 * - O(n) insert/removeAt
 *
 * NOTE: Not final so wrappers (e.g., counting) can extend if needed.
 */
public class Vec<T> {
    private Object[] a;
    private int n;

    public Vec() { this(8); }

    public Vec(int capacity) {
        if (capacity < 0) capacity = 0;
        int cap = 1;
        while (cap < Math.max(1, capacity)) cap <<= 1;
        a = new Object[cap];
        n = 0;
    }

    public int size() { return n; }
    public boolean isEmpty() { return n == 0; }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) a[index];
    }

    public void set(int index, T value) {
        checkIndex(index);
        a[index] = value;
    }

    public void add(T value) {
        ensureCapacity(n + 1);
        a[n++] = value;
    }

    public void insert(int index, T value) {
        if (index < 0 || index > n)
            throw new IndexOutOfBoundsException("insert index " + index + " out of [0," + n + "]");
        ensureCapacity(n + 1);
        for (int i = n; i > index; i--) a[i] = a[i - 1];
        a[index] = value;
        n++;
    }

    @SuppressWarnings("unchecked")
    public T removeAt(int index) {
        checkIndex(index);
        T old = (T) a[index];
        for (int i = index; i < n - 1; i++) a[i] = a[i + 1];
        a[n - 1] = null;
        n--;
        return old;
    }

    public void clear() {
        for (int i = 0; i < n; i++) a[i] = null;
        n = 0;
    }

    private void ensureCapacity(int needed) {
        if (needed <= a.length) return;
        int cap = a.length == 0 ? 1 : a.length;
        while (cap < needed) cap <<= 1;
        Object[] b = new Object[cap];
        for (int i = 0; i < n; i++) b[i] = a[i];
        a = b;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= n)
            throw new IndexOutOfBoundsException("index " + index + " out of [0," + (n - 1) + "]");
    }
}
