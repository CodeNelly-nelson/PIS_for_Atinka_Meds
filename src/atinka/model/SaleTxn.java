package atinka.model;

import java.time.LocalDateTime;

/**
 * Sale transaction (inventory decrease)
 * CSV order we use: timestamp,code,qty,customerId,price,total
 */
public final class SaleTxn {
    private final LocalDateTime timestamp;
    private final String code;
    private final int qty;
    private final String customerId;
    private final double price;
    private final double total;

    public SaleTxn(LocalDateTime timestamp, String code, int qty, String customerId, double price, double total) {
        this.timestamp = timestamp;
        this.code = code;
        this.qty = qty;
        this.customerId = customerId;
        this.price = price;
        this.total = total;
    }

    public LocalDateTime getTimestamp(){ return timestamp; }
    public String getCode(){ return code; }
    public int getQty(){ return qty; }
    public String getCustomerId(){ return customerId; }
    public double getPrice(){ return price; }
    public double getTotal(){ return total; }
}
