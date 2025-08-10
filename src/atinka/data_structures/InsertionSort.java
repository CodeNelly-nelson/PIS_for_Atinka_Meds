package atinka.data_structures;

import java.util.Comparator;
import java.util.List;

public final class InsertionSort {
    private InsertionSort() {}

    public static <T> void sort(List<T> list, Comparator<? super T> cmp) {
        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }
}