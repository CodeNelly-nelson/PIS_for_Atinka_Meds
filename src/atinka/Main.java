package atinka;

import atinka.service.*;
import atinka.storage.*;
import atinka.ui.Router;

import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.model.Supplier;
import atinka.model.Customer;

public final class Main {
    public static void main(String[] args) {
        try {
            // Ensure data folders exist (data/, data/reports/)
            PathsFS.ensure();

            // ---------- Stores (CSV persistence) ----------
            DrugCsvStore drugStore = new DrugCsvStore();
            SupplierCsvStore supplierStore = new SupplierCsvStore();
            CustomerCsvStore customerStore = new CustomerCsvStore();
            PurchaseLogCsv purchaseLog = new PurchaseLogCsv();
            SaleLogCsv saleLog = new SaleLogCsv();

            // ---------- Load from disk ----------
            Vec<Drug> drugsVec       = drugStore.loadAll();
            Vec<Supplier> suppliersVec = supplierStore.loadAll();
            Vec<Customer> customersVec = customerStore.loadAll();

            // ---------- Services ----------
            // DrugService expects initial Vec<Drug>
            DrugService drugService = new DrugService(drugsVec);

            // SupplierService and CustomerService are no-arg; hydrate them
            SupplierService supplierService = new SupplierService();
            for (int i = 0; i < suppliersVec.size(); i++) {
                Supplier s = suppliersVec.get(i);
                if (s != null) supplierService.add(s);  // assumes add(Supplier) exists
            }

            CustomerService customerService = new CustomerService();
            for (int i = 0; i < customersVec.size(); i++) {
                Customer c = customersVec.get(i);
                if (c != null) customerService.add(c);  // assumes add(Customer) exists
            }

            // Inventory service works off DrugService
            InventoryService inventoryService = new InventoryService(drugService);

            // ---------- Router (screens) ----------
            Router router = new Router(
                    drugService,
                    supplierService,
                    customerService,
                    inventoryService,
                    drugStore,
                    supplierStore,
                    customerStore,
                    purchaseLog,
                    saleLog
            );

            router.run();

        } catch (Exception ex) {
            System.out.println("Fatal error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
