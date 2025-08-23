package atinka.service;

import atinka.dsa.Comparator;
import atinka.dsa.HashMapOpen;
import atinka.dsa.MergeSort;
import atinka.dsa.Vec;
import atinka.model.Supplier;

/**
 * SupplierService
 * - Vec store + HashMapOpen index
 * - Filtering by location / turnaround
 */
public final class SupplierService {
    private final Vec<Supplier> list;
    private final HashMapOpen<Supplier> byId;

    public SupplierService(Vec<Supplier> initial){
        this.list = (initial == null) ? new Vec<>() : initial;
        this.byId = new HashMapOpen<>(nextPow2(Math.max(16, this.list.size() * 2)));
        for (int i=0;i<this.list.size();i++){
            Supplier s = this.list.get(i);
            byId.put(s.getId(), s);
        }
    }

    private int nextPow2(int x){ int p=1; while(p<x) p<<=1; return p; }

    public int size(){ return list.size(); }

    public Vec<Supplier> all(){
        Vec<Supplier> c = new Vec<>(list.size());
        for (int i=0;i<list.size();i++) c.add(list.get(i));
        return c;
    }

    public Supplier getById(String id){
        if (id == null) return null;
        return byId.get(id.trim());
    }

    public boolean add(Supplier s){
        if (s == null) return false;
        if (byId.get(s.getId()) != null) return false;
        list.add(s);
        byId.put(s.getId(), s);
        return true;
    }

    public boolean remove(String id){
        Supplier s = getById(id);
        if (s == null) return false;
        // remove from list
        for (int i=0;i<list.size();i++){
            if (list.get(i).getId().equalsIgnoreCase(id)){
                list.removeAt(i);
                break;
            }
        }
        byId.remove(id.trim());
        return true;
    }

    public boolean update(String id, String name, String contact, String location, Integer turnaroundDays){
        Supplier s = getById(id); if (s == null) return false;
        if (name != null && name.trim().length() > 0) s.setName(name);
        if (contact != null) s.setContact(contact);
        if (location != null) s.setLocation(location);
        if (turnaroundDays != null && turnaroundDays.intValue() >= 0) s.setTurnaroundDays(turnaroundDays.intValue());
        return true;
    }

    public Vec<Supplier> filterByLocationContains(String term){
        Vec<Supplier> out = new Vec<>();
        if (term == null || term.trim().length() == 0) return out;
        String n = term.trim().toLowerCase();
        for (int i=0;i<list.size();i++){
            Supplier s = list.get(i);
            if (indexOfIgnoreCase(s.getLocation(), n) >= 0) out.add(s);
        }
        return out;
    }

    public Vec<Supplier> filterByTurnaroundAtMost(int days){
        if (days < 0) days = 0;
        Vec<Supplier> out = new Vec<>();
        for (int i=0;i<list.size();i++){
            Supplier s = list.get(i);
            if (s.getTurnaroundDays() <= days) out.add(s);
        }
        return out;
    }

    public Vec<Supplier> sortedByName(){
        Vec<Supplier> c = all();
        Comparator<Supplier> cmp = (a,b) -> compareIgnoreCase(a.getName(), b.getName());
        MergeSort.sort(c, cmp);
        return c;
    }

    // --------- helpers ---------
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
    private char toLower(char c){ return (c>='A'&&c<='Z')?(char)(c+32):c; }

    private int indexOfIgnoreCase(String hay, String needleLower){
        if (hay == null) return -1;
        int n = hay.length(), m = needleLower.length();
        if (m == 0) return 0;
        for (int i=0;i+m<=n;i++){
            int k=0; while(k<m){
                char a = toLower(hay.charAt(i+k));
                char b = needleLower.charAt(k);
                if (a != b) break; k++;
            }
            if (k==m) return i;
        }
        return -1;
    }
}
