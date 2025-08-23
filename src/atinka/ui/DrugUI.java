package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.model.PurchaseTxn;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.storage.PurchaseLogCsv;
import atinka.util.ConsoleIO;
import atinka.util.Tui;

import java.time.LocalDate;

public final class DrugUI {
    private final DrugService drugs;
    private final InventoryService inv;
    private final PurchaseLogCsv purchaseLog;
    private final SaveHooks saver;

    /** SaveHooks lets the UI trigger persistence without knowing storage details. */
    public interface SaveHooks { void saveDrugs(); }

    public DrugUI(DrugService drugs, InventoryService inv, PurchaseLogCsv purchaseLog, SaveHooks saver){
        this.drugs = drugs;
        this.inv = inv;
        this.purchaseLog = purchaseLog;
        this.saver = saver;
    }

    public void show(){
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Drugs");
            ConsoleIO.printBadges(
                    "MergeSort (list by name/price)",
                    "Linear scan (search/alerts/remove)",
                    "HashMapOpen (lookup/index)",
                    "MinHeap (top-N low stock)",
                    "CSV append (purchases)"
            );

            // Menu (now includes Add new = #3)
            ConsoleIO.println(" 1) List all (by name)           — MergeSort");
            ConsoleIO.println(" 2) List all (by price)          — MergeSort");
            ConsoleIO.println(" 3) Add new                      — O(1) insert + HashMapOpen");
            ConsoleIO.println(" 4) Search name contains         — Linear scan");
            ConsoleIO.println(" 5) Edit / update                — HashMapOpen index");
            ConsoleIO.println(" 6) Link / unlink supplier       — HashMapOpen");
            ConsoleIO.println(" 7) Restock (record purchase)    — O(1) stock adjust; append CSV");
            ConsoleIO.println(" 8) Latest 5 purchases (by time) — MergeSort (time desc)");
            ConsoleIO.println(" 9) Remove by code               — Linear scan + HashMapOpen");
            ConsoleIO.println("10) Low-stock alerts (<= thr)    — Linear scan");
            ConsoleIO.println("11) Top N lowest stock           — MinHeap");
            ConsoleIO.println("\n 0) Back");

            int choice = ConsoleIO.readIntInRange("Choose: ", 0, 11);
            if (choice == 0) return;
            try {
                switch (choice){
                    case 1:  listAllByName();       break;
                    case 2:  listAllByPrice();      break;
                    case 3:  addNew();              break; // <— restored
                    case 4:  searchNameContains();  break;
                    case 5:  edit();                break;
                    case 6:  linkUnlinkSupplier();  break;
                    case 7:  restock();             break;
                    case 8:  latestPurchases();     break;
                    case 9:  remove();              break;
                    case 10: showAlerts();          break;
                    case 11: showTopN();            break;
                }
            } catch (Exception e){
                Tui.toastError("Error: " + e.getMessage());
                pause();
            }
        }
    }

    // -------- actions --------

    private void addNew(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Add Drug");
        ConsoleIO.printBadges("O(1) insert", "HashMapOpen index");
        String code = ConsoleIO.readLineOrCancel("Code");
        if (code == null) return;
        if (drugs.getByCode(code) != null){
            Tui.toastWarn("Code already exists.");
            pause(); return;
        }
        String name = ConsoleIO.readLineOrCancel("Name");
        if (name == null) return;
        double price = ConsoleIO.readPositiveDoubleOrCancel("Price");
        if (Double.isNaN(price)) return;
        int stock = ConsoleIO.readIntOrCancel("Initial stock");
        if (stock == Integer.MIN_VALUE) return;
        String expStr = ConsoleIO.readLine("Expiry date [YYYY-MM-DD] (0=Cancel, empty=none): ");
        if (isCancel(expStr)) return;
        LocalDate expiry = parseDateOrNull(expStr);
        int thr = ConsoleIO.readIntOrCancel("Low-stock threshold");
        if (thr == Integer.MIN_VALUE) return;

        Drug d = new Drug(code.trim(), name.trim(), price, stock, expiry, thr);
        drugs.addDrug(d);
        saver.saveDrugs(); // persist all mutations
        Tui.toastSuccess("Added.");
        pause();
    }

    private void listAllByName(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Drugs — All (by name)");
        ConsoleIO.printBadges("MergeSort");
        Vec<Drug> v = drugs.sortedByName();
        renderTableHeader(true);
        for (int i=0;i<v.size();i++) renderRow(v.get(i));
        pause();
    }

    private void listAllByPrice(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Drugs — All (by price)");
        ConsoleIO.printBadges("MergeSort");
        Vec<Drug> v = drugs.sortedByPrice();
        renderTableHeader(true);
        for (int i=0;i<v.size();i++) renderRow(v.get(i));
        pause();
    }

    private void searchNameContains(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Search — name contains");
        ConsoleIO.printBadges("Linear scan");
        String term = ConsoleIO.readLineOrCancel("Term");
        if (term == null) return;
        Vec<Drug> v = drugs.searchNameContains(term);
        if (v.size() == 0){ Tui.toastInfo("No matches."); pause(); return; }
        renderTableHeader(false);
        for (int i=0;i<v.size();i++) renderRowShort(v.get(i));
        pause();
    }

    private void edit(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Edit Drug");
        ConsoleIO.printBadges("HashMapOpen index");
        String code = ConsoleIO.readLineOrCancel("Code");
        if (code == null) return;
        Drug d = drugs.getByCode(code);
        if (d == null){ Tui.toastWarn("Not found."); pause(); return; }

        ConsoleIO.println("Editing: " + d.getCode() + " / " + d.getName());
        ConsoleIO.println("1) Name");
        ConsoleIO.println("2) Price");
        ConsoleIO.println("3) Stock (absolute set)");
        ConsoleIO.println("4) Expiry date");
        ConsoleIO.println("5) Threshold");
        ConsoleIO.println("0) Cancel");
        int c = ConsoleIO.readIntInRange("Choose: ", 0, 5);
        if (c == 0) return;

        switch (c){
            case 1: {
                String name = ConsoleIO.readLineOrCancel("New name");
                if (name == null) return;
                drugs.updateName(code, name);
                break;
            }
            case 2: {
                double price = ConsoleIO.readPositiveDoubleOrCancel("New price");
                if (Double.isNaN(price)) return;
                drugs.updatePrice(code, price);
                break;
            }
            case 3: {
                int stock = ConsoleIO.readIntOrCancel("New stock");
                if (stock == Integer.MIN_VALUE) return;
                drugs.updateStockAbsolute(code, stock);
                break;
            }
            case 4: {
                String s = ConsoleIO.readLine("New expiry [YYYY-MM-DD] (0=Cancel, empty=none): ");
                if (isCancel(s)) return;
                LocalDate date = parseDateOrNull(s);
                drugs.updateExpiry(code, date);
                break;
            }
            case 5: {
                int thr = ConsoleIO.readIntOrCancel("New threshold");
                if (thr == Integer.MIN_VALUE) return;
                drugs.updateThreshold(code, thr);
                break;
            }
        }
        saver.saveDrugs();
        Tui.toastSuccess("Updated.");
        pause();
    }

    private void linkUnlinkSupplier(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Link / Unlink Supplier");
        ConsoleIO.printBadges("HashMapOpen lookup");
        String code = ConsoleIO.readLineOrCancel("Drug code");
        if (code == null) return;
        Drug d = drugs.getByCode(code);
        if (d == null){ Tui.toastWarn("Drug not found."); pause(); return; }

        ConsoleIO.println("1) Link supplier");
        ConsoleIO.println("2) Unlink supplier");
        ConsoleIO.println("0) Cancel");
        int c = ConsoleIO.readIntInRange("Choose: ", 0, 2);
        if (c == 0) return;

        String sid = ConsoleIO.readLineOrCancel("Supplier ID");
        if (sid == null) return;

        boolean ok = (c == 1) ? drugs.linkSupplier(code, sid) : drugs.unlinkSupplier(code, sid);
        if (ok) { saver.saveDrugs(); Tui.toastSuccess((c==1?"Linked.":"Unlinked.")); }
        else Tui.toastWarn("Operation failed.");
        pause();
    }

    private void restock(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Restock (Purchase)");
        ConsoleIO.printBadges("O(1) stock adjust", "CSV append");
        String code = ConsoleIO.readLineOrCancel("Drug code");
        if (code == null) return;
        int qty = ConsoleIO.readIntOrCancel("Quantity");
        if (qty == Integer.MIN_VALUE) return;
        String buyer = ConsoleIO.readLineOrCancel("Buyer ID");
        if (buyer == null) return;
        double unit = ConsoleIO.readPositiveDoubleOrCancel("Unit cost");
        if (Double.isNaN(unit)) return;

        PurchaseTxn t = inv.recordPurchase(code, qty, buyer, unit);
        purchaseLog.append(t);
        saver.saveDrugs();

        Tui.toastSuccess("Restocked " + qty + " units. Total cost: " + TextPad.toFixed2(t.getTotal()));
        pause();
    }

    private void latestPurchases(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Latest Purchases");
        ConsoleIO.printBadges("MergeSort (time desc)");
        String code = ConsoleIO.readLineOrCancel("Drug code");
        if (code == null) return;
        Vec<PurchaseTxn> v = inv.latestPurchases(code, 5);
        if (v.size() == 0){
            Tui.toastInfo("No purchases recorded for this code.");
            pause(); return;
        }
        ConsoleIO.println(TextPad.padRight("TIME", 19) + "  " +
                TextPad.padLeft("QTY", 5) + "  " +
                TextPad.padLeft("UNIT", 8) + "  " +
                TextPad.padLeft("TOTAL", 10) + "  " +
                TextPad.padRight("BUYER", 10));
        for (int i=0;i<v.size();i++){
            PurchaseTxn t = v.get(i);
            String ts = t.getTimestamp()==null? "" : t.getTimestamp().toString();
            ConsoleIO.println(TextPad.padRight(limit(ts,19),19) + "  " +
                    TextPad.padLeft(String.valueOf(t.getQty()),5) + "  " +
                    TextPad.padLeft(TextPad.toFixed2(t.getUnitCost()),8) + "  " +
                    TextPad.padLeft(TextPad.toFixed2(t.getTotal()),10) + "  " +
                    TextPad.padRight(limit(t.getBuyerId(),10),10));
        }
        pause();
    }

    private void remove(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Remove Drug");
        ConsoleIO.printBadges("Linear scan + HashMapOpen");
        String code = ConsoleIO.readLineOrCancel("Code");
        if (code == null) return;
        boolean ok = drugs.removeByCode(code);
        if (ok){ saver.saveDrugs(); Tui.toastSuccess("Removed."); }
        else Tui.toastWarn("Not found.");
        pause();
    }

    private void showAlerts(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Low-stock Alerts (<= threshold)");
        ConsoleIO.printBadges("Linear scan");
        Vec<Drug> v = inv.currentAlerts();
        if (v.size()==0){ Tui.toastInfo("No alerts."); pause(); return; }
        ConsoleIO.println(TextPad.padRight("CODE", 10) + "  " +
                TextPad.padRight("NAME", 28) + "  " +
                TextPad.padLeft("STOCK", 6) + "  " +
                TextPad.padLeft("THR", 4));
        for (int i=0;i<v.size();i++){
            Drug d = v.get(i);
            ConsoleIO.println(TextPad.padRight(d.getCode(),10) + "  " +
                    TextPad.padRight(limit(d.getName(),28),28) + "  " +
                    TextPad.padLeft(String.valueOf(d.getStock()),6) + "  " +
                    TextPad.padLeft(String.valueOf(d.getThreshold()),4));
        }
        pause();
    }

    private void showTopN(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Top N Lowest Stock");
        ConsoleIO.printBadges("MinHeap");
        int n = ConsoleIO.readIntInRange("N (1..1000): ", 1, 1000);
        Vec<Drug> v = inv.lowStockTopN(n);
        if (v.size()==0){ Tui.toastInfo("No data."); pause(); return; }
        ConsoleIO.println(TextPad.padRight("CODE", 10) + "  " +
                TextPad.padRight("NAME", 28) + "  " +
                TextPad.padLeft("STOCK", 6));
        for (int i=0;i<v.size();i++){
            Drug d = v.get(i);
            ConsoleIO.println(TextPad.padRight(d.getCode(),10) + "  " +
                    TextPad.padRight(limit(d.getName(),28),28) + "  " +
                    TextPad.padLeft(String.valueOf(d.getStock()),6));
        }
        pause();
    }

    // -------- helpers --------

    private boolean isCancel(String s){
        if (s == null) return true;
        s = s.trim();
        return s.equals("0") || s.equalsIgnoreCase("c") || s.equalsIgnoreCase("cancel");
    }

    private void renderTableHeader(boolean showThrAndExpiry){
        if (showThrAndExpiry) {
            ConsoleIO.println(TextPad.padRight("CODE", 10) + "  " +
                    TextPad.padRight("NAME", 28) + "  " +
                    TextPad.padLeft("PRICE", 8) + "  " +
                    TextPad.padLeft("STOCK", 6) + "  " +
                    TextPad.padLeft("THR", 4) + "  " +
                    TextPad.padRight("EXPIRY", 12));
        } else {
            ConsoleIO.println(TextPad.padRight("CODE", 10) + "  " +
                    TextPad.padRight("NAME", 28) + "  " +
                    TextPad.padLeft("PRICE", 8) + "  " +
                    TextPad.padLeft("STOCK", 6));
        }
    }

    private void renderRow(Drug d){
        String exp = d.getExpiry()==null? "" : d.getExpiry().toString();
        ConsoleIO.println(
                TextPad.padRight(d.getCode(),10) + "  " +
                        TextPad.padRight(limit(d.getName(),28),28) + "  " +
                        TextPad.padLeft(TextPad.toFixed2(d.getPrice()),8) + "  " +
                        TextPad.padLeft(String.valueOf(d.getStock()),6) + "  " +
                        TextPad.padLeft(String.valueOf(d.getThreshold()),4) + "  " +
                        TextPad.padRight(exp,12)
        );
    }

    private void renderRowShort(Drug d){
        ConsoleIO.println(
                TextPad.padRight(d.getCode(),10) + "  " +
                        TextPad.padRight(limit(d.getName(),28),28) + "  " +
                        TextPad.padLeft(TextPad.toFixed2(d.getPrice()),8) + "  " +
                        TextPad.padLeft(String.valueOf(d.getStock()),6)
        );
    }

    private LocalDate parseDateOrNull(String s){
        s = s == null ? "" : s.trim();
        if (s.length()==0) return null;
        try { return LocalDate.parse(s); } catch (Exception e){ return null; }
    }

    private String limit(String s, int n){
        if (s == null) return "";
        if (s.length() <= n) return s;
        if (n <= 1) return s.substring(0, n);
        return s.substring(0, n-1) + "…";
    }

    private void pause(){ ConsoleIO.readLine("Press Enter to continue..."); }
}
