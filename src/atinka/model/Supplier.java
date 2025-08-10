package atinka.model;

/** Supplier model without java.util. */
public class Supplier {
    private final String id;     // unique
    private String name;
    private String location;     // e.g., Adenta
    private int turnaroundDays;  // delivery lead time
    private String contact;      // phone/email

    public Supplier(String id, String name, String location, int turnaroundDays, String contact) {
        if (id == null || name == null || location == null) throw new IllegalArgumentException("Null fields not allowed");
        this.id = id.trim();
        this.name = name.trim();
        this.location = location.trim();
        this.turnaroundDays = Math.max(0, turnaroundDays);
        this.contact = contact == null ? "" : contact.trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { if (name == null) throw new IllegalArgumentException(); this.name = name.trim(); }
    public String getLocation() { return location; }
    public void setLocation(String location) { if (location == null) throw new IllegalArgumentException(); this.location = location.trim(); }
    public int getTurnaroundDays() { return turnaroundDays; }
    public void setTurnaroundDays(int d) { this.turnaroundDays = Math.max(0, d); }
    public String getContact() { return contact; }
    public void setContact(String c) { this.contact = c == null ? "" : c.trim(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supplier{")
                .append("id=").append(id)
                .append(", name=").append(name)
                .append(", loc=").append(location)
                .append(", ta=").append(turnaroundDays)
                .append(", contact=").append(contact)
                .append("}");
        return sb.toString();
    }
}