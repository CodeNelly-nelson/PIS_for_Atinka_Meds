package atinka.service;

import atinka.dsa.Comparator;
import atinka.dsa.HashMapOpen;
import atinka.dsa.MergeSort;
import atinka.dsa.Vec;
import atinka.model.Drug;

/**
 * In-memory drug catalog backed by custom Vec + HashMapOpen.
 * Provides sorting, searching, and index-by-code lookups.
 */
public final class DrugService {

    private final Vec<Drug> all;
    private final HashMapOpen<Drug> indexByCode;

    public DrugService(Vec<Drug> initial) {
        this.all = (initial == null) ? new Vec<>() : initial;
        this.indexByCode = new HashMapOpen<>();
        // build index
        for (int i = 0; i < all.size(); i++) {
            Drug d = all.get(i);
            if (d != null && d.getCode() != null) indexByCode.put(d.getCode(), d);
        }
    }

    /** Return the backing Vec (callers usually pass to stores for persistence). */
    public Vec<Drug> all() { return all; }

    /** O(1) avg lookup by code using our custom hash map. */
    public Drug getByCode(String code) {
        if (code == null) return null;
        return indexByCode.get(code);
    }

    /** Adds a new drug; returns false if code already exists. */
    public boolean add(Drug d) {
        if (d == null || d.getCode() == null || d.getCode().length() == 0) return false;
        if (indexByCode.containsKey(d.getCode())) return false;
        all.add(d);
        indexByCode.put(d.getCode(), d);
        return true;
    }

    /** Removes by code; returns true if removed. */
    public boolean remove(String code) {
        if (code == null) return false;
        Drug found = indexByCode.get(code);
        if (found == null) return false;
        // remove from Vec (linear)
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i) == found) {
                all.removeAt(i);
                break;
            }
        }
        indexByCode.remove(code);
        return true;
    }

    /** Update assumes the object reference already lives inside 'all'. */
    public boolean update(Drug d) {
        if (d == null || d.getCode() == null) return false;
        // ensure index points to this reference
        indexByCode.put(d.getCode(), d);
        return true;
    }

    /** Alphabetical by name using our MergeSort. Returns a copy. */
    public Vec<Drug> sortedByName() {
        Vec<Drug> out = copy(all);
        MergeSort.sort(out, new Comparator<Drug>() {
            @Override public int compare(Drug a, Drug b) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
        return out;
    }

    /** By price (asc) using our MergeSort. Returns a copy. */
    public Vec<Drug> sortedByPrice() {
        Vec<Drug> out = copy(all);
        MergeSort.sort(out, new Comparator<Drug>() {
            @Override public int compare(Drug a, Drug b) {
                int c = Double.compare(a.getPrice(), b.getPrice());
                if (c != 0) return c;
                return a.getCode().compareToIgnoreCase(b.getCode());
            }
        });
        return out;
    }

    /** Linear search name contains (case-insensitive). */
    public Vec<Drug> searchNameContains(String term) {
        Vec<Drug> out = new Vec<>();
        if (term == null) return out;
        String n = toLower(term);
        for (int i = 0; i < all.size(); i++) {
            Drug d = all.get(i);
            if (d == null || d.getName() == null) continue;
            if (indexOfIgnoreCase(d.getName(), n) >= 0) out.add(d);
        }
        return out;
    }

    /** All drugs provided by a given supplier id. */
    public Vec<Drug> bySupplier(String supplierId) {
        Vec<Drug> out = new Vec<>();
        if (supplierId == null || supplierId.length() == 0) return out;
        for (int i = 0; i < all.size(); i++) {
            Drug d = all.get(i);
            if (d != null && d.getSupplierIds() != null && d.getSupplierIds().contains(supplierId)) {
                out.add(d);
            }
        }
        return out;
    }

    /** Expose index for performance report comparisons, etc. */
    public HashMapOpen<Drug> indexByCode() { return indexByCode; }

    // ---------- helpers (no java.util) ----------

    private static Vec<Drug> copy(Vec<Drug> src) {
        Vec<Drug> out = new Vec<>(src.size());
        for (int i = 0; i < src.size(); i++) out.add(src.get(i));
        return out;
    }

    /** Lowercase a whole string (ASCII-only; enough for this CLI). */
    private static String toLower(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(toLower(c));
        }
        return sb.toString();
    }

    /** Lowercase a single char (ASCII). */
    private static char toLower(char c) {
        if (c >= 'A' && c <= 'Z') return (char)(c + 32);
        return c;
    }

    /** Simple case-insensitive substring search. */
    private static int indexOfIgnoreCase(String hay, String needle) {
        if (hay == null || needle == null) return -1;
        int n = hay.length(), m = needle.length();
        if (m == 0) return 0;
        for (int i = 0; i + m <= n; i++) {
            int k = 0;
            while (k < m) {
                char a = toLower(hay.charAt(i + k)); // now uses char overload
                char b = needle.charAt(k);           // already lowered by caller
                if (a != b) break;
                k++;
            }
            if (k == m) return i;
        }
        return -1;
    }
}
