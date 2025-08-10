package atinka.model;

import java.time.LocalDate;
import java.util.*;

public class Drug {
    private final String code;              // unique
    private String name;
    private double price;
    private int stock;
    private LocalDate expiry;               // YYYY-MM-DD
    private int reorderThreshold;           // alert when stock < threshold
    private final Set<String> supplierIds = new HashSet<>();

    public Drug(String code, String name, double price, int stock, LocalDate expiry, int reorderThreshold) {
        this.code = Objects.requireNonNull(code).trim();
        this.name = Objects.requireNonNull(name).trim();
        this.price = price;
        this.stock = stock;
        this.expiry = Objects.requireNonNull(expiry);
        this.reorderThreshold = Math.max(0, reorderThreshold);
    }

    // getters / setters
    public String getCode() { return code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name).trim(); }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = Math.max(0, stock); }
    public LocalDate getExpiry() { return expiry; }
    public void setExpiry(LocalDate expiry) { this.expiry = Objects.requireNonNull(expiry); }
    public int getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(int t) { this.reorderThreshold = Math.max(0, t); }
    public Set<String> getSupplierIds() { return supplierIds; }

    public void addSupplier(String supplierId) { if (supplierId != null) supplierIds.add(supplierId.trim()); }
    public void removeSupplier(String supplierId) { if (supplierId != null) supplierIds.remove(supplierId.trim()); }

    public boolean isExpiredAt(LocalDate date) { return !expiry.isAfter(date); }
    public boolean isNearExpiry(LocalDate today, int days) { return !isExpiredAt(today) && !expiry.isAfter(today.plusDays(days)); }

    @Override public boolean equals(Object o) { return (o instanceof Drug d) && d.code.equalsIgnoreCase(code); }
    @Override public int hashCode() { return code.toLowerCase().hashCode(); }
    @Override public String toString() {
        return String.format("Drug{code=%s, name=%s, price=%.2f, stock=%d, expiry=%s, thresh=%d, suppliers=%s}",
                code, name, price, stock, expiry, reorderThreshold, supplierIds);
    }

    public static final Comparator<Drug> BY_NAME = Comparator.comparing(d -> d.name.toLowerCase());
    public static final Comparator<Drug> BY_PRICE = Comparator.comparingDouble(Drug::getPrice);
}