package atinka.model;

/**
 * Customer profile
 *  - id: simple ID like C0001
 *  - name
 *  - contact
 */
public final class Customer {
    private final String id;
    private String name;
    private String contact;

    public Customer(String id, String name, String contact) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("id required");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name required");
        this.id = id.trim();
        this.name = name.trim();
        this.contact = contact == null ? "" : contact.trim();
    }

    public String getId(){ return id; }
    public String getName(){ return name; }
    public String getContact(){ return contact; }

    public void setName(String n){ if (n != null && n.trim().length() > 0) name = n.trim(); }
    public void setContact(String c){ contact = c == null ? "" : c.trim(); }
}
