package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.storage.DrugCsvStore;
import atinka.util.ConsoleIO;

import java.time.LocalDate;

/** All drug flows, isolated from Main (terminal only). */
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
        String code = ConsoleIO.readLine("Drug code (unique): ");
        String name = ConsoleIO.readLine("Name: ");
        double price = ConsoleIO.readPositiveDouble("Unit price: ");
        int stock = ConsoleIO.readIntInRange("Initial stock: ", 0, Integer.MAX_VALUE);
        String expiry = ConsoleIO.readLine("Expiry (YYYY-MM-DD): ");
        int thresh = ConsoleIO.readIntInRange("Reorder threshold: ", 0, Integer.MAX_VALUE);

        Drug d = new Drug(code, name, price, stock, LocalDate.parse(expiry), thresh);
        boolean ok = drugs.addDrug(d);
        if (ok) {
            drugStore.saveAll(drugs.all());
            inventory.rebuildLowStockHeap();
            ConsoleIO.println("Added & saved.");
        } else {
            ConsoleIO.println("Code exists. Not added.");
        }
    }

    private void updateDrugFlow() {
        String code = ConsoleIO.readLine("Drug code to update: ");
        Drug existing = drugs.getByCode(code);
        if (existing == null) { ConsoleIO.println("Not found."); return; }

        String name = ConsoleIO.readLine("Name [" + existing.getName() + "]: ");
        if (!name.isEmpty()) existing.setName(name);

        String p = ConsoleIO.readLine("Price [" + existing.getPrice() + "]: ");
        if (!p.isEmpty()) existing.setPrice(Double.parseDouble(p));

        String s = ConsoleIO.readLine("Stock [" + existing.getStock() + "]: ");
        if (!s.isEmpty()) existing.setStock(Integer.parseInt(s));

        String e = ConsoleIO.readLine("Expiry [" + existing.getExpiry() + "] (YYYY-MM-DD): ");
        if (!e.isEmpty()) existing.setExpiry(LocalDate.parse(e));

        String t = ConsoleIO.readLine("Reorder threshold [" + existing.getReorderThreshold() + "]: ");
        if (!t.isEmpty()) existing.setReorderThreshold(Integer.parseInt(t));

        boolean ok = drugs.updateDrug(existing);
        if (ok) { drugStore.saveAll(drugs.all()); inventory.rebuildLowStockHeap(); ConsoleIO.println("Updated & saved."); }
        else ConsoleIO.println("Update failed.");
    }

    private void removeDrugFlow() {
        String code = ConsoleIO.readLine("Drug code to remove: ");
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
        int ch = ConsoleIO.readIntInRange("Choose: ", 1, 4);
        switch (ch) {
            case 1 -> {
                String code = ConsoleIO.readLine("Enter code: ");
                Drug d = drugs.getByCode(code);
                if (d == null) ConsoleIO.println("Not found.");
                else { Vec<Drug> one = new Vec<>(); one.add(d); renderDrugs(one); }
            }
            case 2 -> {
                String code = ConsoleIO.readLine("Enter code: ");
                Drug d = drugs.findByCodeBinary(code);
                if (d == null) ConsoleIO.println("Not found (binary).");
                else { Vec<Drug> one = new Vec<>(); one.add(d); renderDrugs(one); }
            }
            case 3 -> {
                String term = ConsoleIO.readLine("Name contains: ");
                renderDrugs(drugs.searchByNameContains(term));
            }
            case 4 -> {
                String sup = ConsoleIO.readLine("Supplier ID: ");
                renderDrugs(drugs.searchBySupplier(sup));
            }
        }
    }

    private void linkFlow() {
        String code = ConsoleIO.readLine("Drug code: ");
        String supId = ConsoleIO.readLine("Supplier ID: ");
        int mode = ConsoleIO.readIntInRange("1) Link  2) Unlink: ", 1, 2);
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
                    d.getCode(), d.getName(), d.getPrice(), d.getStock(), d.getExpiry(), d.getReorderThreshold()));
        }
    }
}
