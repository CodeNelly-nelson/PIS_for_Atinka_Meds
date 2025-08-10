package atinka.service;

import atinka.data_structures.LinkedQueue;
import atinka.data_structures.LinkedStack;
import atinka.data_structures.MinHeap;
import atinka.model.*;
import atinka.util.IdGen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** Handles purchases, sales, stock updates, alerts, and basic reports. */
public class InventoryService {
    private final DrugService drugService;

    // Logs
    private final LinkedQueue<PurchaseTxn> purchases = new LinkedQueue<>();
    private final LinkedStack<SaleTxn> sales = new LinkedStack<>();

    // Per-drug ring of most recent purchases (we'll just keep a deque size<=5)
    private final Map<String, Deque<PurchaseTxn>> recentPurchases = new HashMap<>();

    // Low-stock heap (min by stock)
    private MinHeap<Drug> lowStockHeap = new MinHeap<>(Comparator.comparingInt(Drug::getStock));

    public InventoryService(DrugService drugService) {
        this.drugService = drugService;
        rebuildLowStockHeap();
    }

    public PurchaseTxn recordPurchase(String drugCode, int qty, String buyerId, double unitPrice) {
        Drug d = drugService.getByCode(drugCode);
        if (d == null) throw new IllegalArgumentException("Drug not found: " + drugCode);
        if (qty < 0) throw new IllegalArgumentException("Quantity must be >= 0");
        // Update stock & (optionally) price based on purchase price policy (kept simple here)
        d.setStock(d.getStock() + qty);
        // total cost uses given unit price
        double total = unitPrice * qty;
        PurchaseTxn t = new PurchaseTxn(IdGen.nextPurchase(), drugCode, qty, buyerId, LocalDateTime.now(), total);
        purchases.enqueue(t);
        Deque<PurchaseTxn> dq = recentPurchases.computeIfAbsent(drugCode, k -> new ArrayDeque<>());
        dq.addFirst(t); while (dq.size() > 5) dq.removeLast();
        rebuildLowStockHeap();
        return t;
    }

    public SaleTxn recordSale(String drugCode, int qty, String customerId) {
        Drug d = drugService.getByCode(drugCode);
        if (d == null) throw new IllegalArgumentException("Drug not found: " + drugCode);
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        if (d.isExpiredAt(LocalDate.now())) throw new IllegalStateException("Cannot sell expired drug: " + d.getName());
        if (qty > d.getStock()) throw new IllegalStateException("Insufficient stock. Available: " + d.getStock());
        d.setStock(d.getStock() - qty);
        double total = d.getPrice() * qty;
        SaleTxn t = new SaleTxn(IdGen.nextSale(), drugCode, qty, customerId, LocalDateTime.now(), total);
        sales.push(t);
        rebuildLowStockHeap();
        return t;
    }

    public List<PurchaseTxn> latestPurchases(String drugCode, int k) {
        Deque<PurchaseTxn> dq = recentPurchases.getOrDefault(drugCode, new ArrayDeque<>());
        List<PurchaseTxn> out = new ArrayList<>();
        int i = 0; for (PurchaseTxn t : dq) { if (i++ >= k) break; out.add(t); }
        return out;
    }

    public List<SaleTxn> salesBetween(LocalDateTime start, LocalDateTime end) {
        // Since we use a stack, iterate by popping into temp (non-destructive by copy)
        List<SaleTxn> all = new ArrayList<>();
        LinkedStack<SaleTxn> tmp = new LinkedStack<>();
        while (!sales.isEmpty()) { SaleTxn x = sales.pop(); tmp.push(x); }
        while (!tmp.isEmpty()) { SaleTxn x = tmp.pop(); sales.push(x); all.add(x); }
        List<SaleTxn> out = new ArrayList<>();
        for (SaleTxn s : all) if (!s.getTimestamp().isBefore(start) && !s.getTimestamp().isAfter(end)) out.add(s);
        return out;
    }

    public List<Drug> belowThreshold() {
        List<Drug> out = new ArrayList<>();
        for (Drug d : drugService.all()) if (d.getStock() < d.getReorderThreshold()) out.add(d);
        return out;
    }

    public List<Drug> lowStockTopN(int n) {
        // Non-destructive read by copying to a new heap
        MinHeap<Drug> heap = snapshotHeap();
        List<Drug> out = new ArrayList<>();
        for (int i = 0; i < n && !heap.isEmpty(); i++) out.add(heap.extractMin());
        return out;
    }

    public void rebuildLowStockHeap() {
        lowStockHeap = new MinHeap<>(Comparator.comparingInt(Drug::getStock));
        for (Drug d : drugService.all()) lowStockHeap.insert(d);
    }

    private MinHeap<Drug> snapshotHeap() {
        MinHeap<Drug> copy = new MinHeap<>(Comparator.comparingInt(Drug::getStock));
        for (Drug d : drugService.all()) copy.insert(d);
        return copy;
    }
}