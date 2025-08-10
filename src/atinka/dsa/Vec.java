package atinka.dsa;

// Dynamic Array

public final class Vec<T> {
    private Object[] a; private int n;
    public Vec() { this(8); }
    public Vec(int cap) { a = new Object[Math.max(1, cap)]; }

    public int size() { return n; }
    public boolean isEmpty() { return n == 0; }

    @SuppressWarnings("unchecked")
    public T get(int i) { if (i<0||i>=n) throw new IndexOutOfBoundsException(); return (T)a[i]; }

    public void set(int i, T v) { if (i<0||i>=n) throw new IndexOutOfBoundsException(); a[i]=v; }

    public void add(T v) { ensure(n+1); a[n++]=v; }

    public void insert(int i, T v) {
        if (i<0||i>n) throw new IndexOutOfBoundsException(); ensure(n+1);
        for (int k=n; k>i; k--) a[k]=a[k-1]; a[i]=v; n++;
    }

    @SuppressWarnings("unchecked")
    public T removeAt(int i) {
        if (i<0||i>=n) throw new IndexOutOfBoundsException();
        T old=(T)a[i]; for (int k=i; k<n-1; k++) a[k]=a[k+1]; a[--n]=null; return old;
    }

    public void clear() { for(int i=0;i<n;i++) a[i]=null; n=0; }

    private void ensure(int cap) { if (cap<=a.length) return; int m=a.length<<1; while(m<cap) m<<=1; Object[] b=new Object[m]; System.arraycopy(a,0,b,0,n); a=b; }
}