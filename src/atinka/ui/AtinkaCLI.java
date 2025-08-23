package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Customer;
import atinka.model.Drug;
import atinka.model.Supplier;
import atinka.service.CustomerService;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.service.SupplierService;
import atinka.storage.CustomerCsvStore;
import atinka.storage.DrugCsvStore;
import atinka.storage.PurchaseLogCsv;
import atinka.storage.SaleLogCsv;
import atinka.storage.SupplierCsvStore;
import atinka.util.ConsoleIO;
import atinka.util.Tui;

public final class AtinkaCLI {
    // Services
    private final DrugService drugs;
    private final SupplierService suppliers;
    private final CustomerService customers;
    private final InventoryService inventory;

    // Storage
    private final DrugCsvStore drugStore;
    private final SupplierCsvStore supplierStore;
    private final CustomerCsvStore customerStore;
    private final PurchaseLogCsv purchaseLog;
    private final SaleLogCsv saleLog;

    // UIs
    private final DrugUI drugUI;
    private final SupplierUI supplierUI;
    private final CustomerUI customerUI;
    private final SalesUI salesUI;
    private final ReportUI reportUI;

    public AtinkaCLI(DrugService drugs,
                     SupplierService suppliers,
                     CustomerService customers,
                     InventoryService inventory,
                     DrugCsvStore drugStore,
                     SupplierCsvStore supplierStore,
                     CustomerCsvStore customerStore,
                     PurchaseLogCsv purchaseLog,
                     SaleLogCsv saleLog){
        this.drugs = drugs;
        this.suppliers = suppliers;
        this.customers = customers;
        this.inventory = inventory;
        this.drugStore = drugStore;
        this.supplierStore = supplierStore;
        this.customerStore = customerStore;
        this.purchaseLog = purchaseLog;
        this.saleLog = saleLog;

        this.drugUI = new DrugUI(drugs, inventory, purchaseLog, this::saveDrugs);
        this.supplierUI = new SupplierUI(suppliers, this::saveSuppliers);
        this.customerUI = new CustomerUI(customers, this::saveCustomers);
        this.salesUI = new SalesUI(inventory, saleLog, this::saveDrugs);
        this.reportUI = new ReportUI();
        // hand services to ReportUI
        ReportUI.DRUGS = drugs;
        ReportUI.SALES = saleLog;
    }

    public void run(){
        while (true){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Atinka Meds — Main Menu");
            ConsoleIO.println("1) Drugs");
            ConsoleIO.println("2) Suppliers");
            ConsoleIO.println("3) Customers");
            ConsoleIO.println("4) Sales");
            ConsoleIO.println("5) Reports");
            ConsoleIO.println("0) Exit");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 5);
            if (c == 0) {
                Tui.toastInfo("Goodbye.");
                return;
            }
            switch (c){
                case 1: drugUI.show(); break;
                case 2: supplierUI.show(); break;
                case 3: customerUI.show(); break;
                case 4: salesUI.show(); break;
                case 5: reportUI.show(); break;
            }
        }
    }

    // -------- Persistence hooks --------
    private void saveDrugs(){
        Vec<Drug> all = drugs.all();
        drugStore.saveAll(all);
    }
    private void saveSuppliers(){
        Vec<Supplier> all = suppliers.all();
        supplierStore.saveAll(all);
    }
    private void saveCustomers(){
        Vec<Customer> all = customers.all();
        customerStore.saveAll(all);
    }
}
