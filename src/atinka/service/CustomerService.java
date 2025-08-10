package atinka.service;

import atinka.model.Customer;
import atinka.util.IdGen;

import java.util.*;

public class CustomerService {
    private final List<Customer> customers = new ArrayList<>();
    private final Map<String, Customer> byId = new HashMap<>();

    public Customer create(String name, String contact) {
        String id = IdGen.nextCustomer();
        Customer c = new Customer(id, name, contact);
        customers.add(c); byId.put(id, c); return c;
    }

    public boolean add(Customer c) { if (byId.containsKey(c.getId())) return false; customers.add(c); byId.put(c.getId(), c); return true; }

    public boolean update(Customer c) {
        Customer old = byId.get(c.getId()); if (old == null) return false;
        old.setName(c.getName()); old.setContact(c.getContact()); return true;
    }

    public boolean remove(String id) { Customer c = byId.remove(id); if (c == null) return false; customers.remove(c); return true; }

    public Customer get(String id) { return byId.get(id); }

    public List<Customer> all() { return Collections.unmodifiableList(customers); }
}