package atinka.service;

import atinka.dsa.Comparator;
import atinka.dsa.HashMapOpen;
import atinka.dsa.Vec;
import atinka.dsa.MergeSort;
import atinka.model.Drug;

import java.time.LocalDate;

/**
 * DrugService
 * - In-memory store using Vec<Drug>
 * - O(1) avg code→Drug lookup via HashMapOpen
 * - No java.util collections
 */
public final class DrugService {
    private final Vec<Drug> list;             // primary store
    private final HashMapOpen<Drug> byCode;   // index

    public DrugService(Vec<Drug> initial) {
        this.list = (initial == null) ? new Vec<>() : initial;
        this.byCode = new HashMapOpen<>(nextPow2(Math.max(16, this.list.size() * 2)));
        // build index
        for (int i = 0; i < this.list.size(); i++) {
            Drug d = this.list.get(i);
            byCode.put(d.getCode(), d);
        }
    }

    private int nextPow2(int x) { int p=1; while(p<x) p<<=1; return p; }

    // ---------------- Basic accessors ----------------
    public int size(){ return list.size(); }

    /** Returns a COPY to protect internal store. */
    public Vec<Drug> all() {
        Vec<Drug> c = new Vec<>(list.size());
        for (int i=0;i<list.size();i++) c.add(list.get(i));
        return c;
    }

    public Drug getByCode(String code) {
        if (code == null) return null;
        return byCode.get(code.trim());
    }

    public HashMapOpen<Drug> indexByCode(){ return byCode; }

    // ---------------- CRUD ----------------

    public void addDrug(Drug d){
        if (d == null) throw new IllegalArgumentException("Drug is null");
        if (byCode.get(d.getCode()) != null) throw new IllegalStateException("Code exists");
        list.add(d);
        byCode.put(d.getCode(), d);
    }

    public boolean removeByCode(String code){
        if (code == null) return false;
        Drug found = byCode.get(code.trim());
        if (found == null) return false;
        // remove from list (linear scan)
        for (int i=0;i<list.size();i++){
            if (list.get(i).getCode().equalsIgnoreCase(code)){
                list.removeAt(i);
                break;
            }
        }
        byCode.remove(code.trim());
        return true;
    }

    public boolean updateName(String code, String newName){
        Drug d = getByCode(code); if (d == null) return false;
        d.setName(newName);
        return true;
    }

    public boolean updatePrice(String code, double newPrice){
        if (newPrice < 0) throw new IllegalArgumentException("price >= 0");
        Drug d = getByCode(code); if (d == null) return false;
        d.setPrice(newPrice);
        return true;
    }

    public boolean updateStockAbsolute(String code, int newStock){
        if (newStock < 0) throw new IllegalArgumentException("stock >= 0");
        Drug d = getByCode(code); if (d == null) return false;
        d.setStock(newStock);
        return true;
    }

    public boolean updateExpiry(String code, LocalDate expiry){
        Drug d = getByCode(code); if (d == null) return false;
        d.setExpiry(expiry);
        return true;
    }

    public boolean updateThreshold(String code, int t){
        if (t < 0) t = 0;
        Drug d = getByCode(code); if (d == null) return false;
        d.setThreshold(t);
        return true;
    }

    public boolean linkSupplier(String code, String supplierId){
        Drug d = getByCode(code); if (d == null) return false;
        d.addSupplier(supplierId);
        return true;
    }

    public boolean unlinkSupplier(String code, String supplierId){
        Drug d = getByCode(code); if (d == null) return false;
        d.removeSupplier(supplierId);
        return true;
    }

    /** Adjust stock by delta (can be negative). Throws if result < 0. */
    public void adjustStock(String code, int delta){
        Drug d = getByCode(code);
        if (d == null) throw new IllegalStateException("Drug not found");
        int ns = d.getStock() + delta;
        if (ns < 0) throw new IllegalStateException("Stock would go negative");
        d.setStock(ns);
    }

    // ---------------- Sorting / Searching ----------------

    public Vec<Drug> sortedByName(){
        Vec<Drug> c = all();
        Comparator<Drug> byName = (a,b) -> compareIgnoreCase(a.getName(), b.getName());
        MergeSort.sort(c, byName);
        return c;
    }

    public Vec<Drug> sortedByPrice(){
        Vec<Drug> c = all();
        Comparator<Drug> byPrice = (a,b) -> {
            double x = a.getPrice() - b.getPrice();
            if (x < 0) return -1;
            if (x > 0) return 1;
            return compareIgnoreCase(a.getName(), b.getName());
        };
        MergeSort.sort(c, byPrice);
        return c;
    }

    /** Case-insensitive "name contains" search using custom substring match. */
    public Vec<Drug> searchNameContains(String term){
        Vec<Drug> out = new Vec<>();
        if (term == null || term.trim().length() == 0) return out;
        String needle = term.trim().toLowerCase();
        for (int i=0;i<list.size();i++){
            Drug d = list.get(i);
            if (indexOfIgnoreCase(d.getName(), needle) >= 0) out.add(d);
        }
        return out;
    }

    /** All drugs linked to a supplier ID. */
    public Vec<Drug> bySupplier(String supplierId){
        Vec<Drug> out = new Vec<>();
        if (supplierId == null) return out;
        for (int i=0;i<list.size();i++){
            Drug d = list.get(i);
            if (d.hasSupplier(supplierId)) out.add(d);
        }
        return out;
    }

    // ---------------- Helpers (no java.util) ----------------

    private int compareIgnoreCase(String a, String b){
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        int na = a.length(), nb = b.length();
        int n = na < nb ? na : nb;
        for (int i=0;i<n;i++){
            char ca = toLower(a.charAt(i));
            char cb = toLower(b.charAt(i));
            if (ca != cb) return (ca < cb) ? -1 : 1;
        }
        if (na == nb) return 0;
        return (na < nb) ? -1 : 1;
    }

    private char toLower(char c){
        return (c >= 'A' && c <= 'Z') ? (char)(c + 32) : c;
    }

    /** returns index or -1 */
    private int indexOfIgnoreCase(String hay, String needleLower){
        if (hay == null) return -1;
        int n = hay.length(), m = needleLower.length();
        if (m == 0) return 0;
        for (int i=0;i+m<=n;i++){
            int k=0; while(k<m){
                char a = toLower(hay.charAt(i+k));
                char b = needleLower.charAt(k);
                if (a != b) break;
                k++;
            }
            if (k==m) return i;
        }
        return -1;
    }
}
