package atinka.dsa;

public final class Sorting {
    private Sorting(){}

    /** Uses insertion sort for tiny arrays, merge sort otherwise. */
    public static <T> void sort(Vec<T> v, Comparator<T> cmp) {
        int n = v.size();
        if (n < 32) InsertionSort.sort(v, cmp);
        else MergeSort.sort(v, cmp);
    }
}
