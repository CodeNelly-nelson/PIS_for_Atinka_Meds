package atinka.dsa;

/** Simple String→T hash map with linear probing. Not thread-safe. */
public final class HashMapOpen<T> {
    private static final Object TOMBSTONE = new Object();
    private Object[] keys;     // String or TOMBSTONE or null
    private Object[] values;   // T
    private int n;             // number of live entries

    public HashMapOpen(){ this(16); }
    public HashMapOpen(int cap){ int m=1; while(m<cap) m<<=1; keys=new Object[m]; values=new Object[m]; }

    private int mask(){ return keys.length-1; }

    private int slot(String k){
        int h = k.hashCode(); // still uses String.hashCode (allowed; not a collection)
        h ^= (h>>>16);
        return h & mask();
    }

    public int size(){ return n; }

    @SuppressWarnings("unchecked")
    public T get(String k){
        int i=slot(k), m=mask();
        for(int j=0;j<=m;j++){
            Object kk=keys[i];
            if(kk==null) return null; // empty slot
            if(kk!=TOMBSTONE && ((String)kk).equals(k)) return (T)values[i];
            i=(i+1)&m;
        }
        return null;
    }

    public boolean containsKey(String k){ return get(k)!=null; }

    public void put(String k, T v){
        if((n+1)*4 >= keys.length*3) resize(keys.length<<1);
        insert(k,v);
    }

    @SuppressWarnings("unchecked")
    public T remove(String k){
        int i=slot(k), m=mask();
        for(int j=0;j<=m;j++){
            Object kk=keys[i];
            if(kk==null) return null;
            if(kk!=TOMBSTONE && ((String)kk).equals(k)){
                T old=(T)values[i]; keys[i]=TOMBSTONE; values[i]=null; n--; return old;
            }
            i=(i+1)&m;
        }
        return null;
    }

    private void insert(String k, Object v){
        int i=slot(k), m=mask();
        int tomb=-1;
        for(int j=0;j<=m;j++){
            Object kk=keys[i];
            if(kk==null){ if(tomb!=-1) i=tomb; keys[i]=k; values[i]=v; n++; return; }
            if(kk==TOMBSTONE){ if(tomb==-1) tomb=i; }
            else if(((String)kk).equals(k)){ values[i]=v; return; }
            i=(i+1)&m;
        }
    }

    private void resize(int newCap){
        Object[] ok=keys, ov=values; keys=new Object[newCap]; values=new Object[newCap]; n=0;
        for(int i=0;i<ok.length;i++){
            Object kk=ok[i]; if(kk!=null && kk!=TOMBSTONE) insert((String)kk, ov[i]);
        }
    }

    // Simple iterator over entries (index-based) for service loops
    public void forEach(EntryConsumer<T> c){
        for(int i=0;i<keys.length;i++){
            Object kk=keys[i]; if(kk!=null && kk!=TOMBSTONE) c.accept((String)kk, cast(values[i]));
        }
    }
    @SuppressWarnings("unchecked")
    private T cast(Object o){ return (T)o; }

    public interface EntryConsumer<V>{ void accept(String k, V v); }
}