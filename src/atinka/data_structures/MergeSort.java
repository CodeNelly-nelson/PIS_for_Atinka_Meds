package atinka.data_structures;

import java.util.*;

public final class MergeSort {
    private MergeSort() {}

    // Sorts a List<T> using comparator, stable O(n log n)
    public static <T> void sort(List<T> list, Comparator<? super T> cmp) {
        if (list.size() <= 1) return;
        @SuppressWarnings("unchecked")
        T[] a = (T[]) list.toArray();
        mergeSort(a, 0, a.length, cmp);
        for (int i = 0; i < a.length; i++) list.set(i, a[i]);
    }

    private static <T> void mergeSort(T[] a, int lo, int hi, Comparator<? super T> cmp) {
        if (hi - lo <= 1) return;
        int mid = (lo + hi) >>> 1;
        mergeSort(a, lo, mid, cmp);
        mergeSort(a, mid, hi, cmp);
        merge(a, lo, mid, hi, cmp);
    }

    private static <T> void merge(T[] a, int lo, int mid, int hi, Comparator<? super T> cmp) {
        int n1 = mid - lo, n2 = hi - mid;
        @SuppressWarnings("unchecked") T[] L = (T[]) new Object[n1];
        @SuppressWarnings("unchecked") T[] R = (T[]) new Object[n2];
        System.arraycopy(a, lo, L, 0, n1);
        System.arraycopy(a, mid, R, 0, n2);
        int i = 0, j = 0, k = lo;
        while (i < n1 && j < n2) {
            if (cmp.compare(L[i], R[j]) <= 0) a[k++] = L[i++];
            else a[k++] = R[j++];
        }
        while (i < n1) a[k++] = L[i++];
        while (j < n2) a[k++] = R[j++];
    }
}