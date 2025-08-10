package atinka.service;

import atinka.model.Supplier;
import atinka.util.IdGen;

import java.util.*;

public class SupplierService {
    private final List<Supplier> suppliers = new ArrayList<>();
    private final Map<String, Supplier> byId = new HashMap<>();

    public Supplier create(String name, String location, int turnaroundDays, String contact) {
        String id = IdGen.nextSupplier();
        Supplier s = new Supplier(id, name, location, turnaroundDays, contact);
        suppliers.add(s); byId.put(id, s);
        return s;
    }

    public boolean add(Supplier s) {
        if (byId.containsKey(s.getId())) return false;
        suppliers.add(s); byId.put(s.getId(), s); return true;
    }

    public boolean update(Supplier s) {
        Supplier old = byId.get(s.getId()); if (old == null) return false;
        old.setName(s.getName()); old.setLocation(s.getLocation()); old.setTurnaroundDays(s.getTurnaroundDays()); old.setContact(s.getContact());
        return true;
    }

    public boolean remove(String id) {
        Supplier s = byId.remove(id); if (s == null) return false; suppliers.remove(s); return true;
    }

    public Supplier get(String id) { return byId.get(id); }

    public List<Supplier> filterByLocation(String loc) {
        String q = loc.toLowerCase();
        List<Supplier> out = new ArrayList<>();
        for (Supplier s : suppliers) if (s.getLocation().toLowerCase().contains(q)) out.add(s);
        return out;
    }

    public List<Supplier> filterByTurnaroundAtMost(int days) {
        List<Supplier> out = new ArrayList<>();
        for (Supplier s : suppliers) if (s.getTurnaroundDays() <= days) out.add(s);
        return out;
    }

    public List<Supplier> all() { return Collections.unmodifiableList(suppliers); }
}