package atinka.service;

import atinka.data_structures.InsertionSort;
import atinka.data_structures.MergeSort;
import atinka.model.Drug;

import java.util.*;

/** Manages drugs + indices, independent of persistence. */
public class DrugService {
    private final List<Drug> drugs = new ArrayList<>();
    private final Map<String, Drug> byCode = new HashMap<>();

    // supplierId -> drug codes (adjacency list)
    private final Map<String, Set<String>> supplierToDrugs = new HashMap<>();

    public boolean addDrug(Drug d) {
        String code = d.getCode();
        if (byCode.containsKey(code)) return false; // must be unique
        drugs.add(d);
        byCode.put(code, d);
        for (String sup : d.getSupplierIds()) {
            supplierToDrugs.computeIfAbsent(sup, k -> new HashSet<>()).add(code);
        }
        return true;
    }

    public boolean removeDrug(String code) {
        Drug old = byCode.remove(code);
        if (old == null) return false;
        drugs.remove(old);
        for (Set<String> set : supplierToDrugs.values()) set.remove(code);
        return true;
    }

    public boolean updateDrug(Drug updated) {
        Drug existing = byCode.get(updated.getCode());
        if (existing == null) return false;
        // Sync mutable fields
        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setExpiry(updated.getExpiry());
        existing.setReorderThreshold(updated.getReorderThreshold());
        // Rebuild supplier links atomically
        for (Set<String> set : supplierToDrugs.values()) set.remove(existing.getCode());
        for (String sup : updated.getSupplierIds()) {
            supplierToDrugs.computeIfAbsent(sup, k -> new HashSet<>()).add(existing.getCode());
        }
        existing.getSupplierIds().clear();
        existing.getSupplierIds().addAll(updated.getSupplierIds());
        return true;
    }

    public Drug getByCode(String code) { return byCode.get(code); }

    public List<Drug> searchByNameContains(String term) {
        String q = term.toLowerCase();
        List<Drug> out = new ArrayList<>();
        for (Drug d : drugs) if (d.getName().toLowerCase().contains(q)) out.add(d);
        return out;
    }

    public List<Drug> searchBySupplier(String supplierId) {
        Set<String> codes = supplierToDrugs.getOrDefault(supplierId, Collections.emptySet());
        List<Drug> out = new ArrayList<>(codes.size());
        for (String c : codes) { Drug d = byCode.get(c); if (d != null) out.add(d); }
        return out;
    }

    public List<Drug> listSortedByName() {
        List<Drug> copy = new ArrayList<>(drugs);
        if (copy.size() <= 64) InsertionSort.sort(copy, Drug.BY_NAME); else MergeSort.sort(copy, Drug.BY_NAME);
        return copy;
    }

    public List<Drug> listSortedByPrice() {
        List<Drug> copy = new ArrayList<>(drugs);
        if (copy.size() <= 64) InsertionSort.sort(copy, Drug.BY_PRICE); else MergeSort.sort(copy, Drug.BY_PRICE);
        return copy;
    }

    public List<Drug> all() { return Collections.unmodifiableList(drugs); }

    public Map<String, Drug> indexByCode() { return Collections.unmodifiableMap(byCode); }

    public void linkDrugToSupplier(String code, String supplierId) {
        Drug d = byCode.get(code); if (d == null) return;
        d.addSupplier(supplierId);
        supplierToDrugs.computeIfAbsent(supplierId, k -> new HashSet<>()).add(code);
    }

    public void unlinkDrugFromSupplier(String code, String supplierId) {
        Drug d = byCode.get(code); if (d == null) return;
        d.removeSupplier(supplierId);
        Set<String> set = supplierToDrugs.get(supplierId);
        if (set != null) { set.remove(code); if (set.isEmpty()) supplierToDrugs.remove(supplierId); }
    }
}