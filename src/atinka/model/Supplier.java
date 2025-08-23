package atinka.model;

/**
 * Supplier profile
 *  - id: simple ID like S0001
 *  - name
 *  - contact (phone/email)
 *  - location (e.g., "Accra")
 *  - turnaroundDays: delivery time in days
 */
public final class Supplier {
    private final String id;
    private String name;
    private String contact;
    private String location;
    private int turnaroundDays;

    public Supplier(String id, String name, String contact, String location, int turnaroundDays) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("id required");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name required");
        if (turnaroundDays < 0) turnaroundDays = 0;
        this.id = id.trim();
        this.name = name.trim();
        this.contact = contact == null ? "" : contact.trim();
        this.location = location == null ? "" : location.trim();
        this.turnaroundDays = turnaroundDays;
    }

    public String getId(){ return id; }
    public String getName(){ return name; }
    public String getContact(){ return contact; }
    public String getLocation(){ return location; }
    public int getTurnaroundDays(){ return turnaroundDays; }

    public void setName(String n){ if (n != null && n.trim().length() > 0) name = n.trim(); }
    public void setContact(String c){ contact = c == null ? "" : c.trim(); }
    public void setLocation(String l){ location = l == null ? "" : l.trim(); }
    public void setTurnaroundDays(int d){ if (d >= 0) turnaroundDays = d; }
}
