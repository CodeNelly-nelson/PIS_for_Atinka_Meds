package atinka;

import atinka.util.ConsoleIO;
import atinka.model.*;
import atinka.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Atinka Meds — Pharmacy Inventory System (CLI)");
        ConsoleIO.println("Offline-first • No cloud • Transparent files (Step 5)\n");
        Services sv = new Services();
        new MenuRouter(sv).run();
        ConsoleIO.println("\nGoodbye! 👋");
    }
}

class Services {
    final DrugService drugs = new DrugService();
    final SupplierService suppliers = new SupplierService();
    final CustomerService customers = new CustomerService();
    final InventoryService inventory = new InventoryService(drugs);
}

class MenuRouter {
    private final Services sv;
    private boolean running = true;
    MenuRouter(Services sv) { this.sv = sv; }

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
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 3);
            if (c == 0) return;
            switch (c) {
                case 1 -> addDrugFlow();
                case 2 -> listDrugsByName();
                case 3 -> listDrugsByPrice();
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
        LocalDate expiry = LocalDate.parse(expiryStr);
        Drug d = new Drug(code, name, price, stock, expiry, thresh);
        boolean ok = sv.drugs.addDrug(d);
        ConsoleIO.println(ok ? "Added." : "Code already exists. Not added.");
    }

    private void listDrugsByName() {
        List<Drug> list = sv.drugs.listSortedByName();
        if (list.isEmpty()) { ConsoleIO.println("No drugs."); return; }
        ConsoleIO.println(String.format("%-12s %-28s %8s %8s %12s %8s", "CODE", "NAME", "PRICE", "STOCK", "EXPIRY", "THRESH"));
        for (Drug d : list) {
            ConsoleIO.println(String.format("%-12s %-28s %8.2f %8d %12s %8d",
                    d.getCode(), d.getName(), d.getPrice(), d.getStock(), d.getExpiry(), d.getReorderThreshold()));
        }
    }

    private void listDrugsByPrice() {
        List<Drug> list = sv.drugs.listSortedByPrice();
        if (list.isEmpty()) { ConsoleIO.println("No drugs."); return; }
        ConsoleIO.println(String.format("%-12s %-28s %8s %8s %12s %8s", "CODE", "NAME", "PRICE", "STOCK", "EXPIRY", "THRESH"));
        for (Drug d : list) {
            ConsoleIO.println(String.format("%-12s %-28s %8.2f %8d %12s %8d",
                    d.getCode(), d.getName(), d.getPrice(), d.getStock(), d.getExpiry(), d.getReorderThreshold()));
        }
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
        ConsoleIO.println("Added supplier: " + s.getId());
    }

    private void listSuppliers() {
        var list = sv.suppliers.all();
        if (list.isEmpty()) { ConsoleIO.println("No suppliers."); return; }
        ConsoleIO.println(String.format("%-6s %-20s %-16s %6s %-15s", "ID", "NAME", "LOCATION", "TA", "CONTACT"));
        for (Supplier s : list) {
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
        ConsoleIO.println("Added customer: " + cu.getId());
    }

    private void listCustomers() {
        var list = sv.customers.all();
        if (list.isEmpty()) { ConsoleIO.println("No customers."); return; }
        ConsoleIO.println(String.format("%-6s %-24s %-18s", "ID", "NAME", "CONTACT"));
        for (Customer c : list) ConsoleIO.println(String.format("%-6s %-24s %-18s", c.getId(), c.getName(), c.getContact()));
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
        ConsoleIO.println("Recorded purchase: " + t);
    }

    private void latest5PurchasesFlow() {
        String code = ConsoleIO.readLine("Drug code: ");
        var list = sv.inventory.latestPurchases(code, 5);
        if (list.isEmpty()) { ConsoleIO.println("No purchases for this drug."); return; }
        for (PurchaseTxn t : list) ConsoleIO.println(t.toString());
    }

    // ---------------- Sales ----------------
    private void salesMenu() {
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Sales");
            ConsoleIO.println("1) Record sale");
            ConsoleIO.println("2) Sales between dates (summary count)");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 2);
            if (c == 0) return;
            try {
                switch (c) {
                    case 1 -> saleFlow();
                    case 2 -> salesBetweenFlow();
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
        ConsoleIO.println("Recorded sale: " + t);
    }

    private void salesBetweenFlow() {
        String from = ConsoleIO.readLine("From (YYYY-MM-DD): ");
        String to = ConsoleIO.readLine("To   (YYYY-MM-DD): ");
        var list = sv.inventory.salesBetween(LocalDate.parse(from).atStartOfDay(), LocalDate.parse(to).atTime(23,59,59));
        ConsoleIO.println("Sales count: " + list.size());
        double total = 0.0; for (SaleTxn s : list) total += s.getTotal();
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
        var list = sv.inventory.lowStockTopN(n);
        if (list.isEmpty()) { ConsoleIO.println("No drugs."); return; }
        ConsoleIO.println(String.format("%-12s %-28s %8s %8s", "CODE", "NAME", "PRICE", "STOCK"));
        for (Drug d : list) ConsoleIO.println(String.format("%-12s %-28s %8.2f %8d", d.getCode(), d.getName(), d.getPrice(), d.getStock()));
    }

    private void belowThresholdFlow() {
        var list = sv.inventory.belowThreshold();
        if (list.isEmpty()) { ConsoleIO.println("Nothing below threshold."); return; }
        ConsoleIO.println(String.format("%-12s %-28s %8s %8s %8s", "CODE", "NAME", "STOCK", "THRESH", "PRICE"));
        for (Drug d : list) ConsoleIO.println(String.format("%-12s %-28s %8d %8d %8.2f", d.getCode(), d.getName(), d.getStock(), d.getReorderThreshold(), d.getPrice()));
    }

    // ---------------- Reports (placeholder) ----------------
    private void reportsMenu() {
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Reports");
        ConsoleIO.println("TODO: Algorithm performance report (Big-O/Ω) — Step 7\n");
        ConsoleIO.readLine("Press ENTER...");
    }
}