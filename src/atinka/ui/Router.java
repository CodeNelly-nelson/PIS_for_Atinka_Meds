package atinka.ui;

import atinka.util.ConsoleIO;
import atinka.util.Tui;
import atinka.util.Ansi;

import atinka.service.*;
import atinka.storage.*;
import atinka.dsa.Vec;
import atinka.model.Drug;

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
        welcome();
        boolean running = true;

        DrugScreen drugScreen = new DrugScreen(drugs, inventory, drugStore);
        SupplierScreen supplierScreen = new SupplierScreen(suppliers, supplierStore, drugs);
        CustomerScreen customerScreen = new CustomerScreen(customers, customerStore);
        PurchaseScreen purchaseScreen = new PurchaseScreen(drugs, inventory, purchaseLog, drugStore);
        SalesScreen salesScreen = new SalesScreen(drugs, inventory, saleLog, drugStore);
        StockScreen stockScreen = new StockScreen(inventory);
        ReportScreen reportScreen = new ReportScreen(drugs, saleLog);

        while (running) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Pharmacy Inventory (CLI)");
            showAlerts();

            System.out.println(Ansi.color("  [1] Drugs", Ansi.FG_WHITE));
            System.out.println(Ansi.color("  [2] Suppliers", Ansi.FG_WHITE));
            System.out.println(Ansi.color("  [3] Customers", Ansi.FG_WHITE));
            System.out.println(Ansi.color("  [4] Purchases", Ansi.FG_WHITE));
            System.out.println(Ansi.color("  [5] Sales", Ansi.FG_WHITE));
            System.out.println(Ansi.color("  [6] Stock Monitor", Ansi.FG_WHITE));
            System.out.println(Ansi.color("  [7] Reports", Ansi.FG_WHITE));
            System.out.println(Ansi.color("  [0] Exit", Ansi.FG_WHITE));
            Tui.status("Tips: Use numbers to navigate. In prompts: ENTER=cancel, 0=cancel. Colors highlight warnings and success.");

            int c = ConsoleIO.readIntInRange("Choose an option", 0, 7);
            switch (c) {
                case 1 -> drugScreen.run();
                case 2 -> supplierScreen.run();
                case 3 -> customerScreen.run();
                case 4 -> purchaseScreen.run();
                case 5 -> salesScreen.run();
                case 6 -> stockScreen.run();
                case 7 -> reportScreen.run();
                case 0 -> {
                    Boolean ok = ConsoleIO.readYesNoOrCancel("Exit the application?");
                    if (ok != null && ok.booleanValue()) running = false;
                }
            }
        }
    }

    private void welcome() {
        ConsoleIO.clearScreen();
        Tui.banner("Atinka Meds", "Adenta, Accra • Offline-first Pharmacy System");
        Tui.spinner("Loading data", 14, 30);
        System.out.println();
    }

    private void showAlerts() {
        Vec<Drug> low = inventory.belowThreshold();
        if (low != null && low.size() > 0) {
            System.out.println(Ansi.color("⚠ Low stock alerts:", Ansi.FG_YELLOW, Ansi.BOLD));
            for (int i = 0; i < low.size(); i++) {
                Drug d = low.get(i);
                String line = "  - " + d.getCode() + " (" + d.getName() + "): "
                        + d.getStock() + " < threshold " + d.getReorderThreshold();
                System.out.println(Ansi.color(line, Ansi.FG_YELLOW));
            }
            System.out.println();
        }
    }
}
