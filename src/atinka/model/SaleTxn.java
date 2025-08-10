package atinka.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class SaleTxn {
    private final String id;                // unique
    private final String drugCode;
    private final int qty;
    private final String customerId;
    private final LocalDateTime timestamp;
    private final double total;

    public SaleTxn(String id, String drugCode, int qty, String customerId, LocalDateTime ts, double total) {
        this.id = Objects.requireNonNull(id).trim();
        this.drugCode = Objects.requireNonNull(drugCode).trim();
        this.qty = Math.max(0, qty);
        this.customerId = Objects.requireNonNullElse(customerId, "").trim();
        this.timestamp = Objects.requireNonNull(ts);
        this.total = total;
    }

    public String getId() { return id; }
    public String getDrugCode() { return drugCode; }
    public int getQty() { return qty; }
    public String getCustomerId() { return customerId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getTotal() { return total; }

    @Override public String toString() {
        return String.format("SaleTxn{id=%s, drug=%s, qty=%d, customer=%s, time=%s, total=%.2f}",
                id, drugCode, qty, customerId, timestamp, total);
    }
}