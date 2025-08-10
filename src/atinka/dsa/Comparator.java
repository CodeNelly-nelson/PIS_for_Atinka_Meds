package atinka.dsa;

@FunctionalInterface
public interface Comparator<T> {
    int compare(T a, T b); // <0 if a<b, 0 if a==b, >0 if a>b
}