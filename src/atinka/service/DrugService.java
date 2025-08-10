package atinka.service;

import atinka.dsa.*;
import atinka.model.Drug;

/**
 * Drug master service — custom DS only.
 */
public class DrugService {
    private final Vec<Drug> drugs = new Vec<>();
    private final HashMapOpen<Drug> byCode = new HashMapOpen<>();
    private final HashMapOpen<HashSetOpen> supplierToDrugs = new HashMapOpen<>(); // supplierId -> set(drugCode)

    public boolean addDrug(Drug d) {
        if (d == null || d.getCode() == null) return false;
        if (byCode.get(d.getCode()) != null) return false;
        drugs.add(d);
        byCode.put(d.getCode(), d);
        // seed reverse index from existing links
        d.getSupplierIds().forEach(sup -> linkDrugToSupplier(d.getCode(), sup));
        return true;
    }

    public boolean removeDrug(String code) {
        if (code == null) return false;
        Drug old = byCode.remove(code);
        if (old == null) return false;
        // remove from backing vector (linear scan)
        for (int i = 0; i < drugs.size(); i++) {
            if (drugs.get(i).getCode().equalsIgnoreCase(code)) { drugs.removeAt(i); break; }
        }
        // clean reverse index
        supplierToDrugs.forEach((sup,set) -> { if (set != null) set.remove(code); });
        return true;
    }

    public boolean updateDrug(Drug u) {
        if (u == null || u.getCode() == null) return false;
        Drug ex = byCode.get(u.getCode());
        if (ex == null) return false;
        ex.setName(u.getName());
        ex.setPrice(u.getPrice());
        ex.setStock(u.getStock());
        ex.setExpiry(u.getExpiry());
        ex.setReorderThreshold(u.getReorderThreshold());
        // Links managed via link/unlink methods
        return true;
    }

    public Drug getByCode(String code) { return byCode.get(code); }

    public Vec<Drug> searchByNameContains(String term) {
        String q = term == null ? "" : term.toLowerCase();
        Vec<Drug> out = new Vec<>();
        for (int i = 0; i < drugs.size(); i++) {
            Drug d = drugs.get(i);
            String nm = d.getName();
            if (containsIgnoreCase(nm, q)) out.add(d);
        }
        return out;
    }

    public Vec<Drug> searchBySupplier(String supplierId) {
        Vec<Drug> out = new Vec<>();
        HashSetOpen set = supplierToDrugs.get(supplierId);
        if (set == null) return out;
        set.forEach(code -> { Drug d = byCode.get(code); if (d != null) out.add(d); });
        return out;
    }

    public Vec<Drug> listSortedByName() {
        Vec<Drug> v = copyDrugs();
        InsertionSort.sort(v, (a,b) -> a.getName().compareToIgnoreCase(b.getName()));
        return v;
    }

    public Vec<Drug> listSortedByPrice() {
        Vec<Drug> v = copyDrugs();
        InsertionSort.sort(v, (a,b) -> Double.compare(a.getPrice(), b.getPrice()));
        return v;
    }

    public void linkDrugToSupplier(String code, String supplierId) {
        if (code == null || supplierId == null) return;
        Drug d = byCode.get(code); if (d == null) return;
        d.addSupplier(supplierId);
        HashSetOpen set = supplierToDrugs.get(supplierId);
        if (set == null) { set = new HashSetOpen(); supplierToDrugs.put(supplierId, set); }
        set.add(code);
    }

    public void unlinkDrugFromSupplier(String code, String supplierId) {
        if (code == null || supplierId == null) return;
        Drug d = byCode.get(code); if (d == null) return;
        d.removeSupplier(supplierId);
        HashSetOpen set = supplierToDrugs.get(supplierId);
        if (set != null) set.remove(code);
    }

    public Vec<Drug> all() { return copyDrugs(); }
    public HashMapOpen<Drug> indexByCode() { return byCode; }

    // ---------- helpers ----------
    private Vec<Drug> copyDrugs() {
        Vec<Drug> v = new Vec<>(drugs.size());
        for (int i = 0; i < drugs.size(); i++) v.add(drugs.get(i));
        return v;
    }

    private boolean containsIgnoreCase(String hay, String needle) {
        if (needle == null || needle.length() == 0) return true;
        int n = hay.length(), m = needle.length();
        for (int i = 0; i + m <= n; i++) {
            int k = 0;
            while (k < m) {
                char a = toLower(hay.charAt(i + k));
                char b = toLower(needle.charAt(k));
                if (a != b) break; k++;
            }
            if (k == m) return true;
        }
        return false;
    }
    private char toLower(char c) { return (c >= 'A' && c <= 'Z') ? (char)(c + 32) : c; }
}