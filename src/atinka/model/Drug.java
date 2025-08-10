package atinka.model;

import atinka.dsa.HashSetOpen;
import java.time.LocalDate;

/**
 * Domain model for a drug/medicine.
 * Uses custom HashSetOpen for supplier ID links (no java.util collections).
 */
public class Drug {
    private final String code;      // unique
    private String name;
    private double price;
    private int stock;
    private LocalDate expiry;       // YYYY-MM-DD
    private int reorderThreshold;   // alert when stock < threshold

    // Supplier links stored as custom open-addressing set of strings
    private final HashSetOpen supplierIds = new HashSetOpen();

    public Drug(String code, String name, double price, int stock, LocalDate expiry, int reorderThreshold) {
        if (code == null || name == null || expiry == null) throw new IllegalArgumentException("Null fields not allowed");
        this.code = code.trim();
        this.name = name.trim();
        this.price = price;
        this.stock = Math.max(0, stock);
        this.expiry = expiry;
        this.reorderThreshold = Math.max(0, reorderThreshold);
    }

    // --- getters / setters ---
    public String getCode() { return code; }
    public String getName() { return name; }
    public void setName(String name) { if (name == null) throw new IllegalArgumentException(); this.name = name.trim(); }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = Math.max(0, stock); }
    public LocalDate getExpiry() { return expiry; }
    public void setExpiry(LocalDate expiry) { if (expiry == null) throw new IllegalArgumentException(); this.expiry = expiry; }
    public int getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(int t) { this.reorderThreshold = Math.max(0, t); }

    public HashSetOpen getSupplierIds() { return supplierIds; }
    public void addSupplier(String supplierId) { if (supplierId != null) supplierIds.add(supplierId.trim()); }
    public void removeSupplier(String supplierId) { if (supplierId != null) supplierIds.remove(supplierId.trim()); }

    // --- helpers ---
    public boolean isExpiredAt(LocalDate date) { return !expiry.isAfter(date); }
    public boolean isNearExpiry(LocalDate today, int days) {
        return !isExpiredAt(today) && !expiry.isAfter(today.plusDays(days));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Drug{")
                .append("code=").append(code)
                .append(", name=").append(name)
                .append(", price=").append(String.format("%.2f", price))
                .append(", stock=").append(stock)
                .append(", expiry=").append(expiry)
                .append(", thresh=").append(reorderThreshold)
                .append("}");
        return sb.toString();
    }
}