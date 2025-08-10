package atinka.service;

import atinka.dsa.*;
import atinka.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inventory operations — custom DS only.
 * - Purchases: queued (LinkedQueue) + stored in Vec for traversal
 * - Sales:     stack (LinkedStack) + stored in Vec for traversal
 * - Low stock: MinHeap snapshot built from current drugs
 */
public class InventoryService {
    private final DrugService drugService;
    private final LinkedQueue<PurchaseTxn> purchasesQ = new LinkedQueue<>();
    private final Vec<PurchaseTxn> purchasesAll = new Vec<>();
    private final LinkedStack<SaleTxn> salesStack = new LinkedStack<>();
    private final Vec<SaleTxn> salesAll = new Vec<>();
    private MinHeap<Drug> lowStockHeap = new MinHeap<>((a,b) -> Integer.compare(a.getStock(), b.getStock()));

    public InventoryService(DrugService ds) {
        this.drugService = ds;
        rebuildLowStockHeap();
    }

    // ---------- Transactions ----------
    public PurchaseTxn recordPurchase(String drugCode, int qty, String buyerId, double unitPrice) {
        Drug d = drugService.getByCode(drugCode);
        if (d == null) throw new IllegalArgumentException("Drug not found");
        if (qty < 0) throw new IllegalArgumentException("qty>=0");
        d.setStock(d.getStock() + qty);
        PurchaseTxn t = new PurchaseTxn(atinka.util.IdGen.nextPurchase(), drugCode, qty, buyerId, LocalDateTime.now(), unitPrice * qty);
        purchasesQ.enqueue(t); purchasesAll.add(t);
        rebuildLowStockHeap();
        return t;
    }

    public SaleTxn recordSale(String drugCode, int qty, String customerId) {
        Drug d = drugService.getByCode(drugCode);
        if (d == null) throw new IllegalArgumentException("Drug not found");
        if (qty <= 0) throw new IllegalArgumentException("qty>0");
        if (d.isExpiredAt(LocalDate.now())) throw new IllegalStateException("Expired");
        if (qty > d.getStock()) throw new IllegalStateException("Insufficient stock");
        d.setStock(d.getStock() - qty);
        SaleTxn t = new SaleTxn(atinka.util.IdGen.nextSale(), drugCode, qty, customerId, LocalDateTime.now(), d.getPrice() * qty);
        salesStack.push(t); salesAll.add(t);
        rebuildLowStockHeap();
        return t;
    }

    // Most recent N purchases for a specific drug (scan from end)
    public Vec<PurchaseTxn> latestPurchases(String drugCode, int n) {
        Vec<PurchaseTxn> out = new Vec<>();
        int count = 0;
        for (int i = purchasesAll.size() - 1; i >= 0 && count < n; i--) {
            PurchaseTxn t = purchasesAll.get(i);
            if (t.getDrugCode().equalsIgnoreCase(drugCode)) { out.add(t); count++; }
        }
        return out;
    }

    // Sorted purchase history by time
    public Vec<PurchaseTxn> purchasesForDrugSortedByTime(String code, boolean ascending) {
        Vec<PurchaseTxn> tmp = new Vec<>();
        for (int i = 0; i < purchasesAll.size(); i++) {
            PurchaseTxn t = purchasesAll.get(i);
            if (t.getDrugCode().equalsIgnoreCase(code)) tmp.add(t);
        }
        MergeSort.sort(tmp, (a,b) -> {
            int cmp = a.getTimestamp().compareTo(b.getTimestamp());
            return ascending ? cmp : -cmp;
        });
        return tmp;
    }

    // Sales between timestamps (inclusive)
    public Vec<SaleTxn> salesBetween(LocalDateTime from, LocalDateTime to) {
        Vec<SaleTxn> out = new Vec<>();
        for (int i = 0; i < salesAll.size(); i++) {
            SaleTxn s = salesAll.get(i);
            if (!s.getTimestamp().isBefore(from) && !s.getTimestamp().isAfter(to)) out.add(s);
        }
        return out;
    }

    // ---------- Stock monitoring ----------
    public void rebuildLowStockHeap() {
        lowStockHeap = new MinHeap<>((a,b) -> Integer.compare(a.getStock(), b.getStock()));
        Vec<Drug> all = drugService.all();
        for (int i = 0; i < all.size(); i++) lowStockHeap.insert(all.get(i));
    }

    public Vec<Drug> belowThreshold() {
        Vec<Drug> out = new Vec<>();
        Vec<Drug> all = drugService.all();
        for (int i = 0; i < all.size(); i++) {
            Drug d = all.get(i);
            if (d.getStock() < d.getReorderThreshold()) out.add(d);
        }
        return out;
    }

    public Vec<Drug> lowStockTopN(int n) {
        MinHeap<Drug> snap = new MinHeap<>((a,b) -> Integer.compare(a.getStock(), b.getStock()));
        Vec<Drug> all = drugService.all();
        for (int i = 0; i < all.size(); i++) snap.insert(all.get(i));
        Vec<Drug> out = new Vec<>();
        for (int i = 0; i < n; i++) { Drug d = snap.extractMin(); if (d == null) break; out.add(d); }
        return out;
    }
}
