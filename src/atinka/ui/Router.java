package atinka.ui;

import atinka.util.ConsoleIO;
import atinka.service.*;
import atinka.storage.*;
import atinka.dsa.Vec;
import atinka.model.Drug;

/** Top-level router for the terminal app. */
public final class Router {
    private final DrugService drugs;
    private final SupplierService suppliers;
    private final CustomerService customers;
    private final InventoryService inventory;

    private final DrugCsvStore drugStore;
    private final SupplierCsvStore supplierStore;
    private final CustomerCsvStore customerStore;
    private final PurchaseLogCsv purchaseLog;
    private final SaleLogCsv saleLog;

    public Router(DrugService drugs, SupplierService suppliers, CustomerService customers, InventoryService inventory,
                  DrugCsvStore drugStore, SupplierCsvStore supplierStore, CustomerCsvStore customerStore,
                  PurchaseLogCsv purchaseLog, SaleLogCsv saleLog) {
        this.drugs = drugs;
        this.suppliers = suppliers;
        this.customers = customers;
        this.inventory = inventory;
        this.drugStore = drugStore;
        this.supplierStore = supplierStore;
        this.customerStore = customerStore;
        this.purchaseLog = purchaseLog;
        this.saleLog = saleLog;
    }

    public void run() {
        boolean running = true;
        DrugScreen drugScreen = new DrugScreen(drugs, inventory, drugStore);
        SupplierScreen supplierScreen = new SupplierScreen(suppliers, supplierStore);
        CustomerScreen customerScreen = new CustomerScreen(customers, customerStore);
        PurchaseScreen purchaseScreen = new PurchaseScreen(drugs, inventory, purchaseLog, drugStore);
        SalesScreen salesScreen = new SalesScreen(drugs, inventory, saleLog, drugStore);
        StockScreen stockScreen = new StockScreen(inventory);
        ReportScreen reportScreen = new ReportScreen(drugs);

        while (running) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Atinka Meds — Pharmacy Inventory (CLI)");
            showAlerts();
            ConsoleIO.println("1) Drugs");
            ConsoleIO.println("2) Suppliers");
            ConsoleIO.println("3) Customers");
            ConsoleIO.println("4) Purchases");
            ConsoleIO.println("5) Sales");
            ConsoleIO.println("6) Stock Monitor");
            ConsoleIO.println("7) Reports");
            ConsoleIO.println("0) Exit\n");
            int c = ConsoleIO.readIntInRange("Choose an option: ", 0, 7);
            switch (c) {
                case 1 -> drugScreen.run();
                case 2 -> supplierScreen.run();
                case 3 -> customerScreen.run();
                case 4 -> purchaseScreen.run();
                case 5 -> salesScreen.run();
                case 6 -> stockScreen.run();
                case 7 -> reportScreen.run();
                case 0 -> running = false;
            }
        }
    }

    private void showAlerts() {
        Vec<Drug> low = inventory.belowThreshold();
        if (low != null && low.size() > 0) {
            ConsoleIO.println("⚠ Low stock alerts:");
            for (int i = 0; i < low.size(); i++) {
                Drug d = low.get(i);
                ConsoleIO.println("  - " + d.getCode() + " (" + d.getName() + "): " +
                        d.getStock() + " < threshold " + d.getReorderThreshold());
            }
            ConsoleIO.println("");
        }
    }
}
