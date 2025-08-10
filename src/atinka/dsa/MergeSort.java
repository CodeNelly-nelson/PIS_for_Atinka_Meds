package atinka.dsa;

@SuppressWarnings("unchecked")
public final class MergeSort {
    private MergeSort(){}
    public static <T> void sort(Vec<T> v, Comparator<T> cmp){ if(v.size()<=1) return; Object[] a=new Object[v.size()]; for(int i=0;i<v.size();i++) a[i]=v.get(i); Object[] tmp=new Object[a.length]; msort(a,tmp,0,a.length,cmp); for(int i=0;i<a.length;i++) v.set(i,(T)a[i]); }
    private static <T> void msort(Object[] a,Object[] t,int lo,int hi,Comparator<T> c){ if(hi-lo<=1) return; int mid=(lo+hi)>>>1; msort(a,t,lo,mid,c); msort(a,t,mid,hi,c); merge(a,t,lo,mid,hi,c); }
    private static <T> void merge(Object[] a,Object[] t,int lo,int mid,int hi,Comparator<T> c){ int i=lo,j=mid,k=lo; while(i<mid && j<hi){ if(c.compare((T)a[i],(T)a[j])<=0) t[k++]=a[i++]; else t[k++]=a[j++]; } while(i<mid) t[k++]=a[i++]; while(j<hi) t[k++]=a[j++]; for(int x=lo;x<hi;x++) a[x]=t[x]; }
}