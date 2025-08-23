package atinka;

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
import atinka.ui.AtinkaCLI;
import atinka.util.Tui;

/** Entry point for the Atinka Meds console app. */
public final class Main {
    public static void main(String[] args){
        // Storage
        DrugCsvStore drugStore = new DrugCsvStore();
        SupplierCsvStore supplierStore = new SupplierCsvStore();
        CustomerCsvStore customerStore = new CustomerCsvStore();
        PurchaseLogCsv purchaseLog = new PurchaseLogCsv();
        SaleLogCsv saleLog = new SaleLogCsv();

        // Load data
        Vec<Drug> drugsData = drugStore.load();
        Vec<Supplier> suppliersData = supplierStore.load();
        Vec<Customer> customersData = customerStore.load();

        // Services
        DrugService drugs = new DrugService(drugsData);
        SupplierService suppliers = new SupplierService(suppliersData);
        CustomerService customers = new CustomerService(customersData);
        InventoryService inventory = new InventoryService(drugs, purchaseLog, saleLog);

        // App
        Tui.toastInfo("Welcome to Atinka Meds.");
        new AtinkaCLI(drugs, suppliers, customers, inventory,
                drugStore, supplierStore, customerStore, purchaseLog, saleLog).run();
    }
}
