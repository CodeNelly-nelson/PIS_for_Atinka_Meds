package atinka.model;

import java.time.LocalDateTime;

/**
 * Purchase transaction (inventory increase)
 *  - timestamp
 *  - code: drug code
 *  - qty
 *  - buyerId (could be staff or system user)
 *  - unitCost
 *  - total (qty * unitCost)
 */
public final class PurchaseTxn {
    private final LocalDateTime timestamp;
    private final String code;
    private final int qty;
    private final String buyerId;
    private final double unitCost;
    private final double total;

    public PurchaseTxn(LocalDateTime timestamp, String code, int qty, String buyerId, double unitCost, double total) {
        this.timestamp = timestamp;
        this.code = code;
        this.qty = qty;
        this.buyerId = buyerId;
        this.unitCost = unitCost;
        this.total = total;
    }

    public LocalDateTime getTimestamp(){ return timestamp; }
    public String getCode(){ return code; }
    public int getQty(){ return qty; }
    public String getBuyerId(){ return buyerId; }
    public double getUnitCost(){ return unitCost; }
    public double getTotal(){ return total; }
}
