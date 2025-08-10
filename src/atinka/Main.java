package atinka;

import atinka.util.ConsoleIO;

public class Main {
    public static void main(String[] args) {
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Atinka Meds — Pharmacy Inventory System (CLI)");
        ConsoleIO.println("Offline-first • No cloud • Transparent files (coming soon)\n");

        new MenuRouter().run();
        ConsoleIO.println("\nGoodbye! 👋");
    }
}

class MenuRouter {
    private boolean running = true;

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
            ConsoleIO.println("0) Save & Exit\n");

            int choice = ConsoleIO.readIntInRange("Choose an option: ", 0, 7);
            switch (choice) {
                case 1 -> subMenu("Drugs",
                        "Add drug", "Update drug", "Remove drug", "List drugs",
                        "Search (code/name/supplier)", "Sort (name/price)");
                case 2 -> subMenu("Suppliers",
                        "Add supplier", "Update supplier", "Remove supplier", "List suppliers",
                        "Filter (location/turnaround)", "Link drug ↔ supplier");
                case 3 -> subMenu("Customers",
                        "Add customer", "Update customer", "Remove customer", "List customers");
                case 4 -> subMenu("Purchases",
                        "Record purchase", "View history (all)", "View history by drug",
                        "Latest 5 purchases for drug", "Sort history by time");
                case 5 -> subMenu("Sales",
                        "Record sale", "Generate sales report (date range)", "Undo last sale (optional)");
                case 6 -> subMenu("Stock Monitor",
                        "Show low-stock alerts", "Reorder suggestions", "Soon-to-expire items");
                case 7 -> subMenu("Reports",
                        "Algorithm performance (Big‑O/Ω)", "Inventory valuation", "Top sellers");
                case 0 -> running = false;
            }
        }
    }

    private void subMenu(String title, String... options) {
        while (true) {
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader(title);
            for (int i = 0; i < options.length; i++) {
                ConsoleIO.println((i + 1) + ") " + options[i]);
            }
            ConsoleIO.println("0) Back\n");
            int choice = ConsoleIO.readIntInRange("Choose an option: ", 0, options.length);
            if (choice == 0) return;
            ConsoleIO.println("\nTODO → '" + options[choice - 1] + "' will be implemented in the next steps.\n");
            ConsoleIO.readLine("Press ENTER to continue...");
        }
    }
}