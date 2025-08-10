package atinka;

import atinka.util.ConsoleIO;
import atinka.model.*;
import atinka.service.*;
import atinka.storage.*;
import atinka.dsa.Vec;
import atinka.report.PerformanceReport;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        PathsFS.ensure();
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Atinka Meds — Pharmacy Inventory System (CLI)");
        ConsoleIO.println("Offline-first • Custom DS • CSV persistence\n");

        // Storage
        DrugCsvStore drugStore = new DrugCsvStore();
        SupplierCsvStore supplierStore = new SupplierCsvStore();
        CustomerCsvStore customerStore = new CustomerCsvStore();
        PurchaseLogCsv purchaseLog = new PurchaseLogCsv();
        SaleLogCsv saleLog = new SaleLogCsv();

        // Services
        Services sv = new Services(drugStore, supplierStore, customerStore, purchaseLog, saleLog);
        new MenuRouter(sv, drugStore, supplierStore, customerStore, purchaseLog, saleLog).run();
        ConsoleIO.println("\nGoodbye! 👋");
    }
}

class Services {
    final DrugService drugs = new DrugService();
    final SupplierService suppliers = new SupplierService();
    final CustomerService customers = new CustomerService();
    final InventoryService inventory = new InventoryService(drugs);

    Services(DrugCsvStore drugStore, SupplierCsvStore supplierStore, CustomerCsvStore customerStore,
             PurchaseLogCsv purchaseLog, SaleLogCsv saleLog) {
        // Load masters using custom Vec
        Vec<Drug> dAll = drugStore.loadAll();
        for (int i = 0; i < dAll.size(); i++) drugs.addDrug(dAll.get(i));

        Vec<Supplier> sAll = supplierStore.loadAll();
        for (int i = 0; i < sAll.size(); i++) suppliers.add(sAll.get(i));

        Vec<Customer> cAll = customerStore.loadAll();
        for (int i = 0; i < cAll.size(); i++) customers.add(cAll.get(i));

        inventory.rebuildLowStockHeap();
    }
}

class MenuRouter {
    private final Services sv;
    private final DrugCsvStore drugStore;
    private final SupplierCsvStore supplierStore;
    private final CustomerCsvStore customerStore;
    private final PurchaseLogCsv purchaseLog;
    private final SaleLogCsv saleLog;

    private boolean running = true;

    MenuRouter(Services sv, DrugCsvStore d, SupplierCsvStore s, CustomerCsvStore c, PurchaseLogCsv p, SaleLogCsv sl) {
        this.sv = sv; this.drugStore = d; this.supplierStore = s; this.customerStore = c; this.purchaseLog = p; this.saleLog = sl;
    }

    void run() {
        while (running) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Main Menu");
            ConsoleIO.println("1) Drugs");
            ConsoleIO.println("2) Suppliers");
            ConsoleIO.println("3) Customers");
            ConsoleIO.println("4) Purchases");
            ConsoleIO.println("5) Sales");
            ConsoleIO.println("6) Stock Monitor");
            ConsoleIO.println("7) Reports");
            ConsoleIO.println("0) Exit\n");
            int choice = ConsoleIO.readIntInRange("Choose an option: ", 0, 7);
            switch (choice) {
                case 1 -> drugsMenu();
                case 2 -> suppliersMenu();
                case 3 -> customersMenu();
                case 4 -> purchasesMenu();
                case 5 -> salesMenu();
                case 6 -> stockMenu();
                case 7 -> reportsMenu();
                case 0 -> running = false;
            }
        }
    }

    // ---------------- Drugs ----------------
    private void drugsMenu() {
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Drugs");
            ConsoleIO.println("1) Add drug");
            ConsoleIO.println("2) List drugs (by name)");
            ConsoleIO.println("3) List drugs (by price)");
            ConsoleIO.println("4) Search drugs");
            ConsoleIO.println("5) Link/Unlink supplier");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 5);
            if (c == 0) return;
            switch (c) {
                case 1 -> addDrugFlow();
                case 2 -> listDrugsByName();
                case 3 -> listDrugsByPrice();
                case 4 -> searchDrugsFlow();
                case 5 -> linkUnlinkSupplierFlow();
            }
            ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void addDrugFlow() {
        String code = ConsoleIO.readLine("Drug code (unique): ");
        String name = ConsoleIO.readLine("Name: ");
        double price = ConsoleIO.readPositiveDouble("Unit price: ");
        int stock = ConsoleIO.readIntInRange("Initial stock: ", 0, Integer.MAX_VALUE);
        String expiryStr = ConsoleIO.readLine("Expiry (YYYY-MM-DD): ");
        int thresh = ConsoleIO.readIntInRange("Reorder threshold: ", 0, Integer.MAX_VALUE);
        Drug d = new Drug(code, name, price, stock, LocalDate.parse(expiryStr), thresh);
        boolean ok = sv.drugs.addDrug(d);
        if (ok) { drugStore.saveAll(sv.drugs.all()); sv.inventory.rebuildLowStockHeap(); }
        ConsoleIO.println(ok ? "Added & saved." : "Code exists. Not added.");
    }

    private void listDrugsByName() { renderDrugs(sv.drugs.listSortedByName()); }
    private void listDrugsByPrice() { renderDrugs(sv.drugs.listSortedByPrice()); }

    private void renderDrugs(Vec<Drug> list) {
        if (list == null || list.size() == 0) { ConsoleIO.println("No drugs."); return; }
        ConsoleIO.println(String.format("%-12s %-28s %8s %8s %12s %8s", "CODE", "NAME", "PRICE", "STOCK", "EXPIRY", "THRESH"));
        for (int i = 0; i < list.size(); i++) {
            Drug d = list.get(i);
            ConsoleIO.println(String.format("%-12s %-28s %8.2f %8d %12s %8d",
                    d.getCode(), d.getName(), d.getPrice(), d.getStock(), d.getExpiry(), d.getReorderThreshold()));
        }
    }

    private void searchDrugsFlow() {
        ConsoleIO.println("Search by:");
        ConsoleIO.println("1) Code");
        ConsoleIO.println("2) Name contains");
        ConsoleIO.println("3) Supplier ID");
        int ch = ConsoleIO.readIntInRange("Choose: ", 1, 3);
        switch (ch) {
            case 1 -> {
                String code = ConsoleIO.readLine("Enter code: ");
                Drug d = sv.drugs.getByCode(code);
                if (d == null) ConsoleIO.println("Not found.");
                else { Vec<Drug> one = new Vec<>(); one.add(d); renderDrugs(one); }
            }
            case 2 -> {
                String term = ConsoleIO.readLine("Name contains: ");
                renderDrugs(sv.drugs.searchByNameContains(term));
            }
            case 3 -> {
                String supId = ConsoleIO.readLine("Supplier ID: ");
                renderDrugs(sv.drugs.searchBySupplier(supId));
            }
        }
    }

    private void linkUnlinkSupplierFlow() {
        String code = ConsoleIO.readLine("Drug code: ");
        String supId = ConsoleIO.readLine("Supplier ID: ");
        int mode = ConsoleIO.readIntInRange("1) Link  2) Unlink: ", 1, 2);
        if (mode == 1) sv.drugs.linkDrugToSupplier(code, supId); else sv.drugs.unlinkDrugFromSupplier(code, supId);
        drugStore.saveAll(sv.drugs.all());
        ConsoleIO.println(mode == 1 ? "Linked & saved." : "Unlinked & saved.");
    }

    // ---------------- Suppliers ----------------
    private void suppliersMenu() {
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Suppliers");
            ConsoleIO.println("1) Add supplier");
            ConsoleIO.println("2) List suppliers");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 2);
            if (c == 0) return;
            switch (c) {
                case 1 -> addSupplierFlow();
                case 2 -> listSuppliers();
            }
            ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void addSupplierFlow() {
        String name = ConsoleIO.readLine("Name: ");
        String location = ConsoleIO.readLine("Location: ");
        int ta = ConsoleIO.readIntInRange("Turnaround days: ", 0, 365);
        String contact = ConsoleIO.readLine("Contact: ");
        Supplier s = sv.suppliers.create(name, location, ta, contact);
        supplierStore.saveAll(sv.suppliers.all());
        ConsoleIO.println("Added supplier: " + s.getId());
    }

    private void listSuppliers() {
        Vec<Supplier> list = sv.suppliers.all();
        if (list.isEmpty()) { ConsoleIO.println("No suppliers."); return; }
        ConsoleIO.println(String.format("%-6s %-20s %-16s %6s %-15s", "ID", "NAME", "LOCATION", "TA", "CONTACT"));
        for (int i = 0; i < list.size(); i++) {
            Supplier s = list.get(i);
            ConsoleIO.println(String.format("%-6s %-20s %-16s %6d %-15s",
                    s.getId(), s.getName(), s.getLocation(), s.getTurnaroundDays(), s.getContact()));
        }
    }

    // ---------------- Customers ----------------
    private void customersMenu() {
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Customers");
            ConsoleIO.println("1) Add customer");
            ConsoleIO.println("2) List customers");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 2);
            if (c == 0) return;
            switch (c) {
                case 1 -> addCustomerFlow();
                case 2 -> listCustomers();
            }
            ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void addCustomerFlow() {
        String name = ConsoleIO.readLine("Name: ");
        String contact = ConsoleIO.readLine("Contact: ");
        Customer cu = sv.customers.create(name, contact);
        customerStore.saveAll(sv.customers.all());
        ConsoleIO.println("Added customer: " + cu.getId());
    }

    private void listCustomers() {
        Vec<Customer> list = sv.customers.all();
        if (list.isEmpty()) { ConsoleIO.println("No customers."); return; }
        ConsoleIO.println(String.format("%-6s %-24s %-18s", "ID", "NAME", "CONTACT"));
        for (int i = 0; i < list.size(); i++) {
            Customer c = list.get(i);
            ConsoleIO.println(String.format("%-6s %-24s %-18s", c.getId(), c.getName(), c.getContact()));
        }
    }

    // ---------------- Purchases ----------------
    private void purchasesMenu() {
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Purchases");
            ConsoleIO.println("1) Record purchase");
            ConsoleIO.println("2) Latest 5 purchases for a drug");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 2);
            if (c == 0) return;
            try {
                switch (c) {
                    case 1 -> purchaseFlow();
                    case 2 -> latest5PurchasesFlow();
                }
            } catch (Exception ex) {
                ConsoleIO.println("Error: " + ex.getMessage());
            }
            ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void purchaseFlow() {
        String code = ConsoleIO.readLine("Drug code: ");
        int qty = ConsoleIO.readIntInRange("Quantity: ", 0, Integer.MAX_VALUE);
        String buyer = ConsoleIO.readLine("Buyer ID (supplier/customer/ref): ");
        double unitPrice = ConsoleIO.readPositiveDouble("Unit price for this purchase: ");
        PurchaseTxn t = sv.inventory.recordPurchase(code, qty, buyer, unitPrice);
        purchaseLog.append(t);
        drugStore.saveAll(sv.drugs.all()); // persist stock change
        ConsoleIO.println("Recorded & saved purchase: " + t);
    }

    private void latest5PurchasesFlow() {
        String code = ConsoleIO.readLine("Drug code: ");
        Vec<PurchaseTxn> list = sv.inventory.latestPurchases(code, 5);
        if (list == null || list.size() == 0) { ConsoleIO.println("No purchases for this drug."); return; }
        for (int i = 0; i < list.size(); i++) ConsoleIO.println(list.get(i).toString());
    }

    // ---------------- Sales ----------------
    private void salesMenu() {
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Sales");
            ConsoleIO.println("1) Record sale");
            ConsoleIO.println("2) Sales between dates (from log)");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 2);
            if (c == 0) return;
            try {
                switch (c) {
                    case 1 -> saleFlow();
                    case 2 -> salesBetweenFromLogFlow();
                }
            } catch (Exception ex) {
                ConsoleIO.println("Error: " + ex.getMessage());
            }
            ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void saleFlow() {
        String code = ConsoleIO.readLine("Drug code: ");
        int qty = ConsoleIO.readIntInRange("Quantity: ", 1, Integer.MAX_VALUE);
        String cust = ConsoleIO.readLine("Customer ID (optional): ");
        SaleTxn t = sv.inventory.recordSale(code, qty, cust);
        saleLog.append(t);
        drugStore.saveAll(sv.drugs.all()); // persist stock change
        ConsoleIO.println("Recorded & saved sale: " + t);
    }

    private void salesBetweenFromLogFlow() {
        String from = ConsoleIO.readLine("From (YYYY-MM-DD): ");
        String to = ConsoleIO.readLine("To   (YYYY-MM-DD): ");
        LocalDateTime a = LocalDate.parse(from).atStartOfDay();
        LocalDateTime b = LocalDate.parse(to).atTime(23,59,59);
        SaleTxn[] all = saleLog.readAll();
        double total = 0.0; int count = 0;
        for (int i = 0; i < all.length; i++) {
            SaleTxn s = all[i];
            if (!s.getTimestamp().isBefore(a) && !s.getTimestamp().isAfter(b)) { count++; total += s.getTotal(); }
        }
        ConsoleIO.println("Sales count: " + count);
        ConsoleIO.println(String.format("Total value: %.2f", total));
    }

    // ---------------- Stock Monitor ----------------
    private void stockMenu() {
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Stock Monitor");
            ConsoleIO.println("1) Show low-stock (Top N)");
            ConsoleIO.println("2) Show items below threshold");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 2);
            if (c == 0) return;
            switch (c) {
                case 1 -> lowStockTopNFlow();
                case 2 -> belowThresholdFlow();
            }
            ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void lowStockTopNFlow() {
        int n = ConsoleIO.readIntInRange("How many items? ", 1, Integer.MAX_VALUE);
        Vec<Drug> list = sv.inventory.lowStockTopN(n);
        if (list == null || list.size() == 0) { ConsoleIO.println("No drugs."); return; }
        ConsoleIO.println(String.format("%-12s %-28s %8s %8s", "CODE", "NAME", "PRICE", "STOCK"));
        for (int i = 0; i < list.size(); i++) {
            Drug d = list.get(i);
            ConsoleIO.println(String.format("%-12s %-28s %8.2f %8d", d.getCode(), d.getName(), d.getPrice(), d.getStock()));
        }
    }

    private void belowThresholdFlow() {
        Vec<Drug> list = sv.inventory.belowThreshold();
        if (list == null || list.size() == 0) { ConsoleIO.println("Nothing below threshold."); return; }
        ConsoleIO.println(String.format("%-12s %-28s %8s %8s %8s", "CODE", "NAME", "STOCK", "THRESH", "PRICE"));
        for (int i = 0; i < list.size(); i++) {
            Drug d = list.get(i);
            ConsoleIO.println(String.format("%-12s %-28s %8d %8d %8.2f", d.getCode(), d.getName(), d.getStock(), d.getReorderThreshold(), d.getPrice()));
        }
    }

    // ---------------- Reports ----------------
    private void reportsMenu() {
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Reports");
        ConsoleIO.println("1) Generate Algorithm Performance Report");
        ConsoleIO.println("0) Back\n");
        int c = ConsoleIO.readIntInRange("Choose: ", 0, 1);
        if (c == 0) return;
        try {
            java.nio.file.Path path = PerformanceReport.generate(sv.drugs);
            ConsoleIO.println("Report written to: " + path.toAbsolutePath());
        } catch (Exception ex) {
            ConsoleIO.println("Failed to generate report: " + ex.getMessage());
        }
        ConsoleIO.readLine("\nPress ENTER...");
    }
}