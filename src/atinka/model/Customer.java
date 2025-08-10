package atinka.model;

import java.util.Objects;

public class Customer {
    private final String id;                // unique
    private String name;
    private String contact;

    public Customer(String id, String name, String contact) {
        this.id = Objects.requireNonNull(id).trim();
        this.name = Objects.requireNonNull(name).trim();
        this.contact = Objects.requireNonNullElse(contact, "").trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name).trim(); }
    public String getContact() { return contact; }
    public void setContact(String c) { this.contact = Objects.requireNonNullElse(c, "").trim(); }

    @Override public boolean equals(Object o) { return (o instanceof Customer c) && c.id.equalsIgnoreCase(id); }
    @Override public int hashCode() { return id.toLowerCase().hashCode(); }
    @Override public String toString() { return String.format("Customer{id=%s, name=%s, contact=%s}", id, name, contact); }
}