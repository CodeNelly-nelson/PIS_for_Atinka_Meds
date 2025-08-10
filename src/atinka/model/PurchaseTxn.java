package atinka.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class PurchaseTxn {
    private final String id;                // unique
    private final String drugCode;
    private final int qty;
    private final String buyerId;           // supplier or internal buyer code
    private final LocalDateTime timestamp;
    private final double total;

    public PurchaseTxn(String id, String drugCode, int qty, String buyerId, LocalDateTime ts, double total) {
        this.id = Objects.requireNonNull(id).trim();
        this.drugCode = Objects.requireNonNull(drugCode).trim();
        this.qty = Math.max(0, qty);
        this.buyerId = Objects.requireNonNullElse(buyerId, "").trim();
        this.timestamp = Objects.requireNonNull(ts);
        this.total = total;
    }

    public String getId() { return id; }
    public String getDrugCode() { return drugCode; }
    public int getQty() { return qty; }
    public String getBuyerId() { return buyerId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getTotal() { return total; }

    @Override public String toString() {
        return String.format("PurchaseTxn{id=%s, drug=%s, qty=%d, buyer=%s, time=%s, total=%.2f}",
                id, drugCode, qty, buyerId, timestamp, total);
    }
}