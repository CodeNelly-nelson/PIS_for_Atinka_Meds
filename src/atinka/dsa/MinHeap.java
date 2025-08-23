package atinka.dsa;

public final class MinHeap<T> {
    private final Vec<T> a = new Vec<>();
    private final Comparator<T> cmp;

    public MinHeap(Comparator<T> cmp){ this.cmp = cmp; }

    public void insert(T v){
        a.add(v);
        siftUp(a.size() - 1);
    }

    public T peek(){ return a.size() == 0 ? null : a.get(0); }
    public int size(){ return a.size(); }
    public boolean isEmpty(){ return size() == 0; }

    public T extractMin(){
        if (a.size() == 0) return null;
        T min = a.get(0);
        T last = a.removeAt(a.size() - 1);
        if (a.size() > 0) {
            a.set(0, last);
            siftDown(0);
        }
        return min;
    }

    private void siftUp(int i){
        while (i > 0) {
            int p = (i - 1) >> 1;
            if (cmp.compare(a.get(i), a.get(p)) >= 0) break;
            swap(i, p);
            i = p;
        }
    }

    private void siftDown(int i){
        int n = a.size();
        while (true) {
            int l = (i << 1) + 1, r = l + 1, m = i;
            if (l < n && cmp.compare(a.get(l), a.get(m)) < 0) m = l;
            if (r < n && cmp.compare(a.get(r), a.get(m)) < 0) m = r;
            if (m == i) break;
            swap(i, m);
            i = m;
        }
    }

    private void swap(int i, int j){
        T t = a.get(i);
        a.set(i, a.get(j));
        a.set(j, t);
    }
}
