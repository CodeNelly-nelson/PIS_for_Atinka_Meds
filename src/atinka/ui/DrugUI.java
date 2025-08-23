package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.model.PurchaseTxn;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.storage.PurchaseLogCsv;
import atinka.util.ConsoleIO;
import atinka.util.SimpleScreen;
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
            String[] algos = new String[]{ "Vec", "HashMapOpen", "MergeSort", "MinHeap", "Linear scan" };
            String[] body = new String[]{
                    " 1) List all (by name)           — MergeSort",
                    " 2) List all (by price)          — MergeSort",
                    " 3) Search name contains         — Linear scan",
                    " 4) Edit / update                — HashMapOpen index",
                    " 5) Link / unlink supplier       — HashMapOpen",
                    " 6) Restock (record purchase)    — O(1) stock adjust; append CSV",
                    " 7) Latest 5 purchases (by time) — MergeSort (time desc)",
                    " 8) Remove by code               — Linear scan + HashMapOpen",
                    " 9) Low-stock alerts (<= thr)    — Linear scan",
                    "10) Top N lowest stock           — MinHeap",
                    "",
                    " 0) Back"
            };
            SimpleScreen.render("Drugs — Manage", algos, body);

            int choice = ConsoleIO.readIntInRange("Choose: ", 0, 10);
            if (choice == 0) return;
            try {
                switch (choice){
                    case 1: listByName(); break;
                    case 2: listByPrice(); break;
                    case 3: searchNameContains(); break;
                    case 4: edit(); break;
                    case 5: linkUnlinkSupplier(); break;
                    case 6: restock(); break;
                    case 7: latestPurchases(); break;
                    case 8: remove(); break;
                    case 9: showAlerts(); break;
                    case 10: showTopN(); break;
                }
            } catch (Exception e){
                Tui.toastError("Error: " + e.getMessage());
                pause();
            }
        }
    }

    private void listByName(){
        SimpleScreen.render("Drugs — Sorted by Name", new String[]{"MergeSort","Vec"}, new String[0]);
        Vec<Drug> v = drugs.sortedByName();
        tableHeaderPriceStock();
        for (int i=0;i<v.size();i++) rowPriceStock(v.get(i));
        pause();
    }

    private void listByPrice(){
        SimpleScreen.render("Drugs — Sorted by Price", new String[]{"MergeSort","Vec"}, new String[0]);
        Vec<Drug> v = drugs.sortedByPrice();
        tableHeaderPriceStock();
        for (int i=0;i<v.size();i++) rowPriceStock(v.get(i));
        pause();
    }

    private void searchNameContains(){
        SimpleScreen.render("Search — Name Contains", new String[]{"Linear scan","Vec"}, new String[0]);
        String term = ConsoleIO.readLineOrCancel("Term");
        if (term == null) return;
        Vec<Drug> v = drugs.searchNameContains(term);
        if (v.size()==0){ Tui.toastInfo("No matches."); pause(); return; }
        tableHeaderPriceStock();
        for (int i=0;i<v.size();i++) rowPriceStock(v.get(i));
        pause();
    }

    private void edit(){
        SimpleScreen.render("Edit / Update Drug", new String[]{"HashMapOpen index"}, new String[0]);
        String code = ConsoleIO.readLineOrCancel("Code");
        if (code == null) return;
        Drug d = drugs.getByCode(code);
        if (d == null){ Tui.toastWarn("Not found."); pause(); return; }

        ConsoleIO.println("Editing: " + d.getCode() + " / " + d.getName());
        ConsoleIO.println(" 1) Name");
        ConsoleIO.println(" 2) Price");
        ConsoleIO.println(" 3) Stock (absolute set)");
        ConsoleIO.println(" 4) Expiry date");
        ConsoleIO.println(" 5) Threshold");
        ConsoleIO.println(" 0) Cancel");
        int c = ConsoleIO.readIntInRange("Choose: ", 0, 5);
        if (c == 0) return;

        switch (c){
            case 1: { String name = ConsoleIO.readLineOrCancel("New name"); if (name==null) return; drugs.updateName(code, name); } break;
            case 2: { double price = ConsoleIO.readPositiveDoubleOrCancel("New price"); if (Double.isNaN(price)) return; drugs.updatePrice(code, price); } break;
            case 3: { int stock = ConsoleIO.readIntOrCancel("New stock"); if (stock==Integer.MIN_VALUE) return; drugs.updateStockAbsolute(code, stock); } break;
            case 4: {
                String s = ConsoleIO.readLine("New expiry [YYYY-MM-DD] (0=Cancel, empty=none): ");
                if (isCancel(s)) return;
                drugs.updateExpiry(code, parseDateOrNull(s));
            } break;
            case 5: { int thr = ConsoleIO.readIntOrCancel("New threshold"); if (thr==Integer.MIN_VALUE) return; drugs.updateThreshold(code, thr); } break;
        }
        saver.saveDrugs();
        Tui.toastSuccess("Updated.");
        pause();
    }

    private void linkUnlinkSupplier(){
        SimpleScreen.render("Link / Unlink Supplier", new String[]{"HashMapOpen index"}, new String[0]);
        String code = ConsoleIO.readLineOrCancel("Drug code");
        if (code == null) return;
        Drug d = drugs.getByCode(code);
        if (d == null){ Tui.toastWarn("Drug not found."); pause(); return; }

        ConsoleIO.println(" 1) Link supplier");
        ConsoleIO.println(" 2) Unlink supplier");
        ConsoleIO.println(" 0) Cancel");
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
        SimpleScreen.render("Restock (Purchase)", new String[]{"O(1) stock adjust","Append CSV log"}, new String[0]);
        String code = ConsoleIO.readLineOrCancel("Drug code");
        if (code == null) return;
        int qty = ConsoleIO.readIntOrCancel("Quantity"); if (qty==Integer.MIN_VALUE) return;
        String buyer = ConsoleIO.readLineOrCancel("Buyer ID"); if (buyer==null) return;
        double unit = ConsoleIO.readPositiveDoubleOrCancel("Unit cost"); if (Double.isNaN(unit)) return;

        PurchaseTxn t = inv.recordPurchase(code, qty, buyer, unit);
        purchaseLog.append(t); // CSV append
        saver.saveDrugs();

        Tui.toastSuccess("Restocked " + qty + " units. Total cost: " + TextPad.toFixed2(t.getTotal()));
        pause();
    }

    private void latestPurchases(){
        SimpleScreen.render("Latest 5 Purchases (time desc)", new String[]{"MergeSort","Vec"}, new String[0]);
        String code = ConsoleIO.readLineOrCancel("Drug code"); if (code==null) return;
        Vec<PurchaseTxn> v = inv.latestPurchases(code, 5);
        if (v.size()==0){ Tui.toastInfo("No purchases recorded."); pause(); return; }

        ConsoleIO.println(TextPad.padRight("TIME", 19) + "  " +
                TextPad.padLeft("QTY", 5) + "  " +
                TextPad.padLeft("UNIT", 8) + "  " +
                TextPad.padLeft("TOTAL",10) + "  " +
                TextPad.padRight("BUYER",10));
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
        SimpleScreen.render("Remove Drug", new String[]{"Linear scan (Vec)","HashMapOpen index"}, new String[0]);
        String code = ConsoleIO.readLineOrCancel("Code"); if (code==null) return;
        boolean ok = drugs.removeByCode(code);
        if (ok){ saver.saveDrugs(); Tui.toastSuccess("Removed."); } else Tui.toastWarn("Not found.");
        pause();
    }

    private void showAlerts(){
        SimpleScreen.render("Low-stock Alerts (<= threshold)", new String[]{"Linear scan","Vec"}, new String[0]);
        Vec<Drug> v = inv.currentAlerts();
        if (v.size()==0){ Tui.toastInfo("No alerts."); pause(); return; }
        ConsoleIO.println(TextPad.padRight("CODE",10) + "  " +
                TextPad.padRight("NAME",28) + "  " +
                TextPad.padLeft("STOCK",6) + "  " +
                TextPad.padLeft("THR",4));
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
        SimpleScreen.render("Top N Lowest Stock", new String[]{"MinHeap"}, new String[0]);
        int n = ConsoleIO.readIntInRange("N (1..1000): ", 1, 1000);
        Vec<Drug> v = inv.lowStockTopN(n);
        if (v.size()==0){ Tui.toastInfo("No data."); pause(); return; }
        ConsoleIO.println(TextPad.padRight("CODE",10) + "  " +
                TextPad.padRight("NAME",28) + "  " +
                TextPad.padLeft("STOCK",6));
        for (int i=0;i<v.size();i++){
            Drug d = v.get(i);
            ConsoleIO.println(TextPad.padRight(d.getCode(),10) + "  " +
                    TextPad.padRight(limit(d.getName(),28),28) + "  " +
                    TextPad.padLeft(String.valueOf(d.getStock()),6));
        }
        pause();
    }

    // ---- table helpers
    private void tableHeaderPriceStock(){
        ConsoleIO.println(TextPad.padRight("CODE",10) + "  " +
                TextPad.padRight("NAME",28) + "  " +
                TextPad.padLeft("PRICE",8) + "  " +
                TextPad.padLeft("STOCK",6) + "  " +
                TextPad.padLeft("THR",4) + "  " +
                TextPad.padRight("EXPIRY",12));
    }
    private void rowPriceStock(Drug d){
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

    // ---- utils
    private boolean isCancel(String s){
        if (s == null) return true;
        s = s.trim();
        return s.equals("0") || s.equalsIgnoreCase("c") || s.equalsIgnoreCase("cancel");
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
