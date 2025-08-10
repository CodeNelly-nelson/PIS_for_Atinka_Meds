package atinka.model;

/** Customer model without java.util. */
public class Customer {
    private final String id;   // unique
    private String name;
    private String contact;

    public Customer(String id, String name, String contact) {
        if (id == null || name == null) throw new IllegalArgumentException("Null fields not allowed");
        this.id = id.trim();
        this.name = name.trim();
        this.contact = contact == null ? "" : contact.trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { if (name == null) throw new IllegalArgumentException(); this.name = name.trim(); }
    public String getContact() { return contact; }
    public void setContact(String c) { this.contact = c == null ? "" : c.trim(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer{")
                .append("id=").append(id)
                .append(", name=").append(name)
                .append(", contact=").append(contact)
                .append("}");
        return sb.toString();
    }
}