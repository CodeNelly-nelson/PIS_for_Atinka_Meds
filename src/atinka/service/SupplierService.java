package atinka.service;

import atinka.dsa.*;
import atinka.model.Supplier;
import atinka.util.IdGen;

/** Supplier service — custom DS only. */
public class SupplierService {
    private final Vec<Supplier> suppliers = new Vec<>();
    private final HashMapOpen<Supplier> byId = new HashMapOpen<>();

    public Supplier create(String name, String location, int ta, String contact) {
        String id = IdGen.nextSupplier();
        Supplier s = new Supplier(id, name, location, ta, contact);
        suppliers.add(s); byId.put(id, s); return s;
    }

    public boolean add(Supplier s) {
        if (s == null || byId.get(s.getId()) != null) return false;
        suppliers.add(s); byId.put(s.getId(), s); return true;
    }

    public boolean update(Supplier s) {
        Supplier o = byId.get(s.getId()); if (o == null) return false;
        o.setName(s.getName()); o.setLocation(s.getLocation()); o.setTurnaroundDays(s.getTurnaroundDays()); o.setContact(s.getContact());
        return true;
    }

    public boolean remove(String id) {
        Supplier o = byId.remove(id); if (o == null) return false;
        for (int i = 0; i < suppliers.size(); i++) { if (suppliers.get(i).getId().equals(id)) { suppliers.removeAt(i); break; } }
        return true;
    }

    public Supplier get(String id) { return byId.get(id); }

    public Vec<Supplier> filterByLocation(String q) {
        String qq = q == null ? "" : q.toLowerCase();
        Vec<Supplier> out = new Vec<>();
        for (int i = 0; i < suppliers.size(); i++) {
            Supplier s = suppliers.get(i);
            if (containsIgnoreCase(s.getLocation(), qq)) out.add(s);
        }
        return out;
    }

    public Vec<Supplier> filterByTurnaroundAtMost(int days) {
        Vec<Supplier> out = new Vec<>();
        for (int i = 0; i < suppliers.size(); i++) {
            Supplier s = suppliers.get(i); if (s.getTurnaroundDays() <= days) out.add(s);
        }
        return out;
    }

    public Vec<Supplier> all() {
        Vec<Supplier> v = new Vec<>(suppliers.size());
        for (int i = 0; i < suppliers.size(); i++) v.add(suppliers.get(i));
        return v;
    }

    // simple contains ignore-case (ASCII)
    private boolean containsIgnoreCase(String hay, String needle) {
        if (needle == null || needle.length() == 0) return true;
        int n = hay.length(), m = needle.length();
        for (int i = 0; i + m <= n; i++) {
            int k = 0; while (k < m) {
                char a = toLower(hay.charAt(i + k)); char b = toLower(needle.charAt(k));
                if (a != b) break; k++; }
            if (k == m) return true;
        }
        return false;
    }
    private char toLower(char c){ return (c>='A'&&c<='Z')?(char)(c+32):c; }
}