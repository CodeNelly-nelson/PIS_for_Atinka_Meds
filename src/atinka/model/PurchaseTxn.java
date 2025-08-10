package atinka.model;

import java.time.LocalDateTime;

/** Purchase transaction, no java.util. */
public class PurchaseTxn {
    private final String id;        // unique
    private final String drugCode;
    private final int qty;
    private final String buyerId;   // supplier or internal buyer code
    private final LocalDateTime timestamp;
    private final double total;

    public PurchaseTxn(String id, String drugCode, int qty, String buyerId, LocalDateTime ts, double total) {
        if (id == null || drugCode == null || ts == null) throw new IllegalArgumentException("Null fields not allowed");
        this.id = id.trim();
        this.drugCode = drugCode.trim();
        this.qty = Math.max(0, qty);
        this.buyerId = buyerId == null ? "" : buyerId.trim();
        this.timestamp = ts;
        this.total = total;
    }

    public String getId() { return id; }
    public String getDrugCode() { return drugCode; }
    public int getQty() { return qty; }
    public String getBuyerId() { return buyerId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getTotal() { return total; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PurchaseTxn{")
                .append("id=").append(id)
                .append(", drug=").append(drugCode)
                .append(", qty=").append(qty)
                .append(", buyer=").append(buyerId)
                .append(", time=").append(timestamp)
                .append(", total=").append(String.format("%.2f", total))
                .append("}");
        return sb.toString();
    }
}