package atinka.model;

import java.util.Objects;

public class Supplier {
    private final String id;                // unique
    private String name;
    private String location;               // e.g., Adenta
    private int turnaroundDays;            // delivery lead time
    private String contact;                // phone/email

    public Supplier(String id, String name, String location, int turnaroundDays, String contact) {
        this.id = Objects.requireNonNull(id).trim();
        this.name = Objects.requireNonNull(name).trim();
        this.location = Objects.requireNonNull(location).trim();
        this.turnaroundDays = Math.max(0, turnaroundDays);
        this.contact = Objects.requireNonNullElse(contact, "").trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name).trim(); }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = Objects.requireNonNull(location).trim(); }
    public int getTurnaroundDays() { return turnaroundDays; }
    public void setTurnaroundDays(int d) { this.turnaroundDays = Math.max(0, d); }
    public String getContact() { return contact; }
    public void setContact(String c) { this.contact = Objects.requireNonNullElse(c, "").trim(); }

    @Override public boolean equals(Object o) { return (o instanceof Supplier s) && s.id.equalsIgnoreCase(id); }
    @Override public int hashCode() { return id.toLowerCase().hashCode(); }
    @Override public String toString() {
        return String.format("Supplier{id=%s, name=%s, loc=%s, ta=%d, contact=%s}", id, name, location, turnaroundDays, contact);
    }
}