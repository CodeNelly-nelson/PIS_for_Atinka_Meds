// File: src/atinka/Main.java
package atinka;

import atinka.util.ConsoleIO;
import atinka.storage.*;
import atinka.service.*;
import atinka.model.*;
import atinka.dsa.Vec;
import atinka.ui.Router;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        // Ensure data folders/files exist
        PathsFS.ensure();

        // Banner
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Atinka Meds — Pharmacy Inventory System (CLI)");
        ConsoleIO.println("Offline-first • Custom DS • CSV persistence\n");

        // ---------- Storage ----------
        DrugCsvStore drugStore         = new DrugCsvStore();
        SupplierCsvStore supplierStore = new SupplierCsvStore();
        CustomerCsvStore customerStore = new CustomerCsvStore();
        PurchaseLogCsv purchaseLog     = new PurchaseLogCsv();
        SaleLogCsv saleLog             = new SaleLogCsv();

        // ---------- Services (custom DS only) ----------
        DrugService drugService         = new DrugService();
        SupplierService supplierService = new SupplierService();
        CustomerService customerService = new CustomerService();
        InventoryService inventory      = new InventoryService(drugService);

        // ---------- Load CSV → Services (Vec-based, no java.util) ----------
        Vec<Drug> drugs = drugStore.loadAll();
        for (int i = 0; i < drugs.size(); i++) {
            drugService.addDrug(drugs.get(i));
        }

        Vec<Supplier> sups = supplierStore.loadAll();
        for (int i = 0; i < sups.size(); i++) {
            supplierService.add(sups.get(i));
        }

        Vec<Customer> custs = customerStore.loadAll();
        for (int i = 0; i < custs.size(); i++) {
            customerService.add(custs.get(i));
        }

        // Build initial low-stock heap
        inventory.rebuildLowStockHeap();

        // ---------- Hand off to the terminal router ----------
        new Router(
                drugService, supplierService, customerService, inventory,
                drugStore, supplierStore, customerStore, purchaseLog, saleLog
        ).run();

        ConsoleIO.println("\nGoodbye! 👋");
    }
}
