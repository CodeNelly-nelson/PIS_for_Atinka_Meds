package atinka.service;

import atinka.dsa.HashMapOpen;
import atinka.dsa.Vec;
import atinka.model.Customer;

/** CustomerService: Vec store + HashMapOpen index. */
public final class CustomerService {
    private final Vec<Customer> list;
    private final HashMapOpen<Customer> byId;

    public CustomerService(Vec<Customer> initial){
        this.list = (initial == null) ? new Vec<>() : initial;
        this.byId = new HashMapOpen<>(nextPow2(Math.max(16, this.list.size() * 2)));
        for (int i=0;i<this.list.size();i++){
            Customer c = this.list.get(i);
            byId.put(c.getId(), c);
        }
    }

    private int nextPow2(int x){ int p=1; while(p<x) p<<=1; return p; }

    public int size(){ return list.size(); }

    public Vec<Customer> all(){
        Vec<Customer> c = new Vec<>(list.size());
        for (int i=0;i<list.size();i++) c.add(list.get(i));
        return c;
    }

    public Customer getById(String id){
        if (id == null) return null;
        return byId.get(id.trim());
    }

    public boolean add(Customer c){
        if (c == null) return false;
        if (byId.get(c.getId()) != null) return false;
        list.add(c);
        byId.put(c.getId(), c);
        return true;
    }

    public boolean remove(String id){
        Customer c = getById(id);
        if (c == null) return false;
        for (int i=0;i<list.size();i++){
            if (list.get(i).getId().equalsIgnoreCase(id)){
                list.removeAt(i);
                break;
            }
        }
        byId.remove(id.trim());
        return true;
    }

    public boolean update(String id, String name, String contact){
        Customer c = getById(id); if (c == null) return false;
        if (name != null && name.trim().length() > 0) c.setName(name);
        if (contact != null) c.setContact(contact);
        return true;
    }
}
