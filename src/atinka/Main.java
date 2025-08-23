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
import atinka.util.Ansi;
import atinka.util.ConsoleIO;
import atinka.util.SimpleScreen;
import atinka.util.Tui;

/** Entry point for the Atinka Meds console app. */
public final class Main {
    public static void main(String[] args){
        splash();  // pretty, centered, algorithm badges

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

        // Small operational summary
        ConsoleIO.hr();
        ConsoleIO.println((Ansi.isEnabled()? Ansi.bold() : "") + "Loaded:" + Ansi.reset());
        ConsoleIO.println(" • Drugs:     " + drugsData.size());
        ConsoleIO.println(" • Suppliers: " + suppliersData.size());
        ConsoleIO.println(" • Customers: " + customersData.size());
        ConsoleIO.hr();
        Tui.toastInfo("Welcome to Atinka Meds.");

        // App
        new AtinkaCLI(
                drugs, suppliers, customers, inventory,
                drugStore, supplierStore, customerStore, purchaseLog, saleLog
        ).run();
    }

    /** Centered, high-contrast splash with algorithm badges. */
    private static void splash(){
        String[] algos = new String[]{
                "Vec", "HashMapOpen", "MergeSort", "BinarySearch", "MinHeap", "CSV append"
        };
        String[] body = new String[]{
                "Data Flow:",
                "  CSV Stores → Vec collections → Services → CLI Screens",
                "",
                "Tips:",
                " - Press 0 at any input to cancel/return",
                " - Colors auto-disable on unsupported terminals"
        };

        ConsoleIO.clearScreen();
        String title = " ATINKA MEDS ";
        String line = repeat('═', Math.max(48, title.length() + 4));

        if (Ansi.isEnabled()){
            printlnC(Ansi.fg("green") + line + Ansi.reset());
            printlnC(Ansi.bold() + Ansi.fg("cyan") + title + Ansi.reset());
            printlnC(Ansi.fg("green") + line + Ansi.reset());
            ConsoleIO.println("");
        } else {
            printlnC(line.replace('═','='));
            printlnC(title);
            printlnC(line.replace('═','='));
            ConsoleIO.println("");
        }

        // Render with header + algorithm badges
        SimpleScreen.render("Atinka Meds — Console", algos, body);
    }

    private static void printlnC(String s){ ConsoleIO.println(s); }
    private static String repeat(char c, int n){
        if (n <= 0) return "";
        StringBuilder b = new StringBuilder(n);
        for (int i=0;i<n;i++) b.append(c);
        return b.toString();
    }
}
