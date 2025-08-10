package atinka.dsa;

public final class BinarySearch {
    private BinarySearch(){}
    public static <T> int indexOf(Vec<T> v, T key, Comparator<T> cmp){
        int lo=0, hi=v.size()-1; while(lo<=hi){ int mid=(lo+hi)>>>1; int c=cmp.compare(v.get(mid), key); if(c==0) return mid; if(c<0) lo=mid+1; else hi=mid-1; } return -1;
    }
}