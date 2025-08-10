package atinka.dsa;

public final class HashSetOpen {
    private static final Object PRESENT = new Object();
    private final HashMapOpen<Object> map = new HashMapOpen<>();
    public void add(String k){ map.put(k, PRESENT); }
    public boolean contains(String k){ return map.get(k)!=null; }
    public void remove(String k){ map.remove(k); }
    public int size(){ return map.size(); }
    public void forEach(StringConsumer c){ map.forEach((k,v)->c.accept(k)); }
    public interface StringConsumer{ void accept(String s); }
}