package atinka.model;

import atinka.dsa.HashSetOpen;
import java.time.LocalDate;

/**
 * Domain model: Drug
 * Fields:
 *  - code: unique SKU (String)
 *  - name
 *  - price (selling price)
 *  - stock (units on hand)
 *  - expiry (LocalDate)
 *  - threshold (reorder alert level)
 *  - supplierIds: set of supplier IDs linked to this drug
 */
public final class Drug {
    private final String code;
    private String name;
    private double price;
    private int stock;
    private LocalDate expiry;
    private int threshold;
    private final HashSetOpen supplierIds = new HashSetOpen();

    public Drug(String code, String name, double price, int stock, LocalDate expiry, int threshold) {
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("code required");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name required");
        if (price < 0) throw new IllegalArgumentException("price >= 0");
        if (stock < 0) throw new IllegalArgumentException("stock >= 0");
        if (threshold < 0) threshold = 0;
        this.code = code.trim();
        this.name = name.trim();
        this.price = price;
        this.stock = stock;
        this.expiry = expiry;
        this.threshold = threshold;
    }

    public String getCode(){ return code; }
    public String getName(){ return name; }
    public double getPrice(){ return price; }
    public int getStock(){ return stock; }
    public LocalDate getExpiry(){ return expiry; }
    public int getThreshold(){ return threshold; }

    public void setName(String v){ if (v != null && v.trim().length() > 0) name = v.trim(); }
    public void setPrice(double v){ if (v >= 0) price = v; }
    public void setStock(int v){ if (v >= 0) stock = v; }
    public void setExpiry(LocalDate d){ expiry = d; }
    public void setThreshold(int t){ if (t >= 0) threshold = t; }

    public boolean isExpired(LocalDate today){
        if (expiry == null) return false;
        return today != null && !expiry.isAfter(today);
    }

    public boolean isLowStock(){
        return stock <= threshold;
    }

    // ---- Supplier links ----
    public void addSupplier(String supplierId){
        if (supplierId != null && supplierId.trim().length() > 0)
            supplierIds.add(supplierId.trim());
    }
    public void removeSupplier(String supplierId){
        if (supplierId != null) supplierIds.remove(supplierId);
    }
    public boolean hasSupplier(String supplierId){
        return supplierId != null && supplierIds.contains(supplierId);
    }
    public HashSetOpen suppliers(){ return supplierIds; }
}
