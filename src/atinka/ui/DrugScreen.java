package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.storage.DrugCsvStore;
import atinka.util.ConsoleIO;
import atinka.util.DateUtil;

import java.time.LocalDate;

/** All drug flows (terminal only). Every prompt supports 0 = cancel. */
public final class DrugScreen extends Screen {
    private final DrugService drugs;
    private final InventoryService inventory;
    private final DrugCsvStore drugStore;

    public DrugScreen(DrugService drugs, InventoryService inventory, DrugCsvStore drugStore) {
        this.drugs = drugs;
        this.inventory = inventory;
        this.drugStore = drugStore;
    }

    @Override public void run() {
        boolean back = false;
        while (!back) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Drugs");
            ConsoleIO.println("1) Add drug");
            ConsoleIO.println("2) List drugs (by name)");
            ConsoleIO.println("3) List drugs (by price)");
            ConsoleIO.println("4) Search drugs");
            ConsoleIO.println("5) Link/Unlink supplier");
            ConsoleIO.println("6) Update drug");
            ConsoleIO.println("7) Remove drug");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 7);
            switch (c) {
                case 1 -> addDrugFlow();
                case 2 -> renderDrugs(drugs.listSortedByName());
                case 3 -> renderDrugs(drugs.listSortedByPrice());
                case 4 -> searchFlow();
                case 5 -> linkFlow();
                case 6 -> updateDrugFlow();
                case 7 -> removeDrugFlow();
                case 0 -> back = true;
            }
            if (!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void addDrugFlow() {
        try {
            String code = ConsoleIO.readLineOrCancel("Drug code (unique)");
            if (code == null) { ConsoleIO.println("Cancelled."); return; }

            String name = ConsoleIO.readLineOrCancel("Name");
            if (name == null) { ConsoleIO.println("Cancelled."); return; }

            double price = ConsoleIO.readPositiveDoubleOrCancel("Unit price");
            if (price == Double.NEGATIVE_INFINITY) { ConsoleIO.println("Cancelled."); return; }

            int stock = ConsoleIO.readIntOrCancel("Initial stock");
            if (stock == Integer.MIN_VALUE) { ConsoleIO.println("Cancelled."); return; }

            String expiryIn = ConsoleIO.readLineOrCancel("Expiry (YYYY-MM-DD)");
            if (expiryIn == null) { ConsoleIO.println("Cancelled."); return; }
            LocalDate expiry = DateUtil.parseDateOrNull(expiryIn);
            if (expiry == null) { ConsoleIO.println("Invalid date. Use YYYY-MM-DD."); return; }

            int thresh = ConsoleIO.readIntOrCancel("Reorder threshold");
            if (thresh == Integer.MIN_VALUE) { ConsoleIO.println("Cancelled."); return; }

            Drug d = new Drug(code, name, price, stock, expiry, thresh);
            boolean ok = drugs.addDrug(d);
            if (ok) {
                drugStore.saveAll(drugs.all());
                inventory.rebuildLowStockHeap();
                ConsoleIO.println("Added & saved.");
            } else {
                ConsoleIO.println("Code exists. Not added.");
            }
        } catch (Exception ex) {
            ConsoleIO.println("Error: " + ex.getMessage());
        }
    }

    private void updateDrugFlow() {
        try {
            String code = ConsoleIO.readLineOrCancel("Drug code to update");
            if (code == null) { ConsoleIO.println("Cancelled."); return; }
            Drug existing = drugs.getByCode(code);
            if (existing == null) { ConsoleIO.println("Not found."); return; }

            ConsoleIO.println("Enter new values or leave empty to keep current. Enter 0 to cancel the whole update.");

            String name = ConsoleIO.readLineOrCancel("Name [" + existing.getName() + "]");
            if (name == null) { ConsoleIO.println("Cancelled."); return; }
            if (!name.isEmpty()) existing.setName(name);

            String p = ConsoleIO.readLineOrCancel("Price [" + existing.getPrice() + "]");
            if (p == null) { ConsoleIO.println("Cancelled."); return; }
            if (!p.isEmpty()) {
                try { existing.setPrice(Double.parseDouble(p)); } catch (Exception e) { ConsoleIO.println("Invalid price."); return; }
            }

            String s = ConsoleIO.readLineOrCancel("Stock [" + existing.getStock() + "]");
            if (s == null) { ConsoleIO.println("Cancelled."); return; }
            if (!s.isEmpty()) {
                try { existing.setStock(Integer.parseInt(s)); } catch (Exception e) { ConsoleIO.println("Invalid stock."); return; }
            }

            String e = ConsoleIO.readLineOrCancel("Expiry [" + existing.getExpiry() + "] (YYYY-MM-DD)");
            if (e == null) { ConsoleIO.println("Cancelled."); return; }
            if (!e.isEmpty()) {
                LocalDate d = DateUtil.parseDateOrNull(e);
                if (d == null) { ConsoleIO.println("Invalid date."); return; }
                existing.setExpiry(d);
            }

            String t = ConsoleIO.readLineOrCancel("Reorder threshold [" + existing.getReorderThreshold() + "]");
            if (t == null) { ConsoleIO.println("Cancelled."); return; }
            if (!t.isEmpty()) {
                try { existing.setReorderThreshold(Integer.parseInt(t)); } catch (Exception ex) { ConsoleIO.println("Invalid threshold."); return; }
            }

            boolean ok = drugs.updateDrug(existing);
            if (ok) { drugStore.saveAll(drugs.all()); inventory.rebuildLowStockHeap(); ConsoleIO.println("Updated & saved."); }
            else ConsoleIO.println("Update failed.");
        } catch (Exception ex) {
            ConsoleIO.println("Error: " + ex.getMessage());
        }
    }

    private void removeDrugFlow() {
        String code = ConsoleIO.readLineOrCancel("Drug code to remove");
        if (code == null) { ConsoleIO.println("Cancelled."); return; }
        boolean ok = drugs.removeDrug(code);
        if (ok) { drugStore.saveAll(drugs.all()); inventory.rebuildLowStockHeap(); ConsoleIO.println("Removed & saved."); }
        else ConsoleIO.println("Not found; nothing removed.");
    }

    private void searchFlow() {
        ConsoleIO.println("Search by:");
        ConsoleIO.println("1) Code (Hash index)");
        ConsoleIO.println("2) Code (Binary search)");
        ConsoleIO.println("3) Name contains");
        ConsoleIO.println("4) Supplier ID");
        ConsoleIO.println("0) Back\n");
        int ch = ConsoleIO.readIntInRange("Choose: ", 0, 4);
        if (ch == 0) { ConsoleIO.println("Cancelled."); return; }
        switch (ch) {
            case 1 -> {
                String code = ConsoleIO.readLineOrCancel("Enter code");
                if (code == null) { ConsoleIO.println("Cancelled."); return; }
                Drug d = drugs.getByCode(code);
                if (d == null) ConsoleIO.println("Not found.");
                else { Vec<Drug> one = new Vec<>(); one.add(d); renderDrugs(one); }
            }
            case 2 -> {
                String code = ConsoleIO.readLineOrCancel("Enter code");
                if (code == null) { ConsoleIO.println("Cancelled."); return; }
                Drug d = drugs.findByCodeBinary(code);
                if (d == null) ConsoleIO.println("Not found (binary).");
                else { Vec<Drug> one = new Vec<>(); one.add(d); renderDrugs(one); }
            }
            case 3 -> {
                String term = ConsoleIO.readLineOrCancel("Name contains");
                if (term == null) { ConsoleIO.println("Cancelled."); return; }
                renderDrugs(drugs.searchByNameContains(term));
            }
            case 4 -> {
                String sup = ConsoleIO.readLineOrCancel("Supplier ID");
                if (sup == null) { ConsoleIO.println("Cancelled."); return; }
                renderDrugs(drugs.searchBySupplier(sup));
            }
        }
    }

    private void linkFlow() {
        String code = ConsoleIO.readLineOrCancel("Drug code");
        if (code == null) { ConsoleIO.println("Cancelled."); return; }
        String supId = ConsoleIO.readLineOrCancel("Supplier ID");
        if (supId == null) { ConsoleIO.println("Cancelled."); return; }
        int mode = ConsoleIO.readIntInRange("1) Link  2) Unlink  0) Cancel: ", 0, 2);
        if (mode == 0) { ConsoleIO.println("Cancelled."); return; }
        if (mode == 1) drugs.linkDrugToSupplier(code, supId);
        else drugs.unlinkDrugFromSupplier(code, supId);
        drugStore.saveAll(drugs.all());
        ConsoleIO.println(mode == 1 ? "Linked & saved." : "Unlinked & saved.");
    }

    private void renderDrugs(Vec<Drug> list) {
        if (list == null || list.size() == 0) { ConsoleIO.println("No drugs."); return; }
        ConsoleIO.println(String.format("%-12s %-28s %8s %8s %12s %8s",
                "CODE", "NAME", "PRICE", "STOCK", "EXPIRY", "THRESH"));
        for (int i = 0; i < list.size(); i++) {
            Drug d = list.get(i);
            ConsoleIO.println(String.format("%-12s %-28s %8.2f %8d %12s %8d",
                    d.getCode(), d.getName(), d.getPrice(), d.getStock(),
                    d.getExpiry()==null?"":d.getExpiry().toString(),
                    d.getReorderThreshold()));
        }
    }
}
