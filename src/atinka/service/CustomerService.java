package atinka.service;

import atinka.dsa.*;
import atinka.model.Customer;
import atinka.util.IdGen;

/** Customer service — custom DS only. */
public class CustomerService {
    private final Vec<Customer> customers = new Vec<>();
    private final HashMapOpen<Customer> byId = new HashMapOpen<>();

    public Customer create(String name, String contact) {
        String id = IdGen.nextCustomer();
        Customer c = new Customer(id, name, contact);
        customers.add(c); byId.put(id, c); return c;
    }

    public boolean add(Customer c) {
        if (c == null || byId.get(c.getId()) != null) return false;
        customers.add(c); byId.put(c.getId(), c); return true;
    }

    public boolean update(Customer c) {
        Customer o = byId.get(c.getId()); if (o == null) return false;
        o.setName(c.getName()); o.setContact(c.getContact());
        return true;
    }

    public boolean remove(String id) {
        Customer o = byId.remove(id); if (o == null) return false;
        for (int i = 0; i < customers.size(); i++) { if (customers.get(i).getId().equals(id)) { customers.removeAt(i); break; } }
        return true;
    }

    public Customer get(String id) { return byId.get(id); }

    public Vec<Customer> all() {
        Vec<Customer> v = new Vec<>(customers.size());
        for (int i = 0; i < customers.size(); i++) v.add(customers.get(i));
        return v;
    }
}