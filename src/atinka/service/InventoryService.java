package atinka.service;

import atinka.dsa.Comparator;
import atinka.dsa.MergeSort;
import atinka.dsa.MinHeap;
import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.model.PurchaseTxn;
import atinka.model.SaleTxn;
import atinka.storage.PurchaseLogCsv;
import atinka.storage.SaleLogCsv;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * InventoryService
 * - Business rules for sales & purchases
 * - Low-stock priority via MinHeap
 * - Latest N purchases per drug
 */
public final class InventoryService {
    private final DrugService drugs;
    private final PurchaseLogCsv purchaseLog;
    private final SaleLogCsv saleLog;

    public InventoryService(DrugService drugs, PurchaseLogCsv purchaseLog, SaleLogCsv saleLog){
        this.drugs = drugs;
        this.purchaseLog = purchaseLog;
        this.saleLog = saleLog;
    }

    /** Record a sale; throws on expired or insufficient stock. */
    public SaleTxn recordSale(String code, int qty, String customerId){
        if (qty <= 0) throw new IllegalArgumentException("qty > 0");
        Drug d = drugs.getByCode(code);
        if (d == null) throw new IllegalStateException("Drug not found");
        LocalDate today = LocalDate.now();
        if (d.isExpired(today)) throw new IllegalStateException("Expired");
        if (d.getStock() < qty) throw new IllegalStateException("Insufficient stock");

        // Update inventory
        drugs.adjustStock(code, -qty);

        double price = d.getPrice();
        double total = price * qty;
        LocalDateTime ts = LocalDateTime.now();
        SaleTxn st = new SaleTxn(ts, code, qty, customerId == null ? "" : customerId.trim(), price, total);

        // NOTE: The UI currently appends to saleLog and saves drugs.
        // We keep return-only here to match the UI's call pattern.
        return st;
    }

    /** Record a purchase (restock). */
    public PurchaseTxn recordPurchase(String code, int qty, String buyerId, double unitCost){
        if (qty <= 0) throw new IllegalArgumentException("qty > 0");
        if (unitCost < 0) throw new IllegalArgumentException("unitCost >= 0");
        Drug d = drugs.getByCode(code);
        if (d == null) throw new IllegalStateException("Drug not found");
        drugs.adjustStock(code, qty);
        double total = unitCost * qty;
        PurchaseTxn pt = new PurchaseTxn(LocalDateTime.now(), code, qty, buyerId == null? "" : buyerId.trim(), unitCost, total);
        // Caller (UI) should append to log and persist drugs.
        return pt;
    }

    /** Top N lowest stock drugs via MinHeap. */
    public Vec<Drug> lowStockTopN(int n){
        if (n <= 0) n = 1;
        Vec<Drug> all = drugs.all();
        Comparator<Drug> byStock = (a,b) -> {
            int x = a.getStock() - b.getStock();
            if (x != 0) return (x < 0) ? -1 : 1;
            return compareIgnoreCase(a.getName(), b.getName());
        };
        MinHeap<Drug> heap = new MinHeap<>(byStock);
        for (int i=0;i<all.size();i++) heap.insert(all.get(i));
        int take = n < all.size() ? n : all.size();
        Vec<Drug> res = new Vec<>(take);
        for (int i=0;i<take;i++){
            Drug d = heap.extractMin();
            if (d == null) break;
            res.add(d);
        }
        return res;
    }

    /** Alert list of drugs currently at/below threshold. */
    public Vec<Drug> currentAlerts(){
        Vec<Drug> all = drugs.all();
        Vec<Drug> out = new Vec<>();
        for (int i=0;i<all.size();i++){
            if (all.get(i).isLowStock()) out.add(all.get(i));
        }
        return out;
    }

    /** Latest k purchases for a code, sorted by time DESC. */
    public Vec<PurchaseTxn> latestPurchases(String code, int k){
        if (k <= 0) k = 5;
        Vec<PurchaseTxn> all = purchaseLog.readAll();
        // filter
        Vec<PurchaseTxn> filt = new Vec<>();
        for (int i=0;i<all.size();i++){
            PurchaseTxn t = all.get(i);
            if (t.getCode() != null && t.getCode().equalsIgnoreCase(code)) filt.add(t);
        }
        // sort DESC by timestamp
        Comparator<PurchaseTxn> byTimeDesc = (a,b) -> compareTimeDesc(a.getTimestamp(), b.getTimestamp());
        MergeSort.sort(filt, byTimeDesc);
        // take first k
        int take = k < filt.size() ? k : filt.size();
        Vec<PurchaseTxn> out = new Vec<>(take);
        for (int i=0;i<take;i++) out.add(filt.get(i));
        return out;
    }

    // ------------- helpers -------------

    private int compareIgnoreCase(String a, String b){
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        int na = a.length(), nb = b.length();
        int n = na < nb ? na : nb;
        for (int i=0;i<n;i++){
            char ca = toLower(a.charAt(i));
            char cb = toLower(b.charAt(i));
            if (ca != cb) return (ca < cb) ? -1 : 1;
        }
        if (na == nb) return 0;
        return (na < nb) ? -1 : 1;
    }
    private char toLower(char c){ return (c>='A'&&c<='Z')?(char)(c+32):c; }

    private int compareTimeDesc(java.time.LocalDateTime a, java.time.LocalDateTime b){
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        // want DESC: newer first
        if (a.isAfter(b)) return -1;
        if (a.isBefore(b)) return 1;
        return 0;
    }
}
