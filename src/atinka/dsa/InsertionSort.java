package atinka.dsa;

public final class InsertionSort {
    private InsertionSort(){}
    public static <T> void sort(Vec<T> v, Comparator<T> cmp){
        for(int i=1;i<v.size();i++){
            T key=v.get(i); int j=i-1;
            while(j>=0 && cmp.compare(v.get(j), key)>0){ v.set(j+1, v.get(j)); j--; }
            v.set(j+1, key);
        }
    }
}