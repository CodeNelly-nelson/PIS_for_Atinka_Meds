package atinka.dsa;

public final class SelectionSort {
    private SelectionSort(){}

    public static <T> void sort(Vec<T> v, Comparator<T> cmp) {
        int n = v.size();
        for (int i = 0; i < n - 1; i++) {
            int best = i;
            for (int j = i + 1; j < n; j++) {
                if (cmp.compare(v.get(j), v.get(best)) < 0) best = j;
            }
            if (best != i) swap(v, i, best);
        }
    }

    private static <T> void swap(Vec<T> v, int i, int j) {
        T t = v.get(i);
        v.set(i, v.get(j));
        v.set(j, t);
    }
}
