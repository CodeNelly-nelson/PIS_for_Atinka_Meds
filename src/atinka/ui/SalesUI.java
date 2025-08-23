package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.SaleTxn;
import atinka.service.InventoryService;
import atinka.storage.SaleLogCsv;
import atinka.util.ConsoleIO;
import atinka.util.Tui;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class SalesUI {
    private final InventoryService inv;
    private final SaleLogCsv saleLog;
    private final SaveHooks saver;

    public interface SaveHooks {
        void saveDrugs(); // inventory changes after sale
    }

    public SalesUI(InventoryService inv, SaleLogCsv saleLog, SaveHooks saver){
        this.inv = inv; this.saleLog = saleLog; this.saver = saver;
    }

    public void show(){
        while (true){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Sales");
            ConsoleIO.println("1) Record sale");
            ConsoleIO.println("2) Today's totals");
            ConsoleIO.println("3) Totals for month (YYYY-MM)");
            ConsoleIO.println("0) Back");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 3);
            if (c == 0) return;
            try {
                switch (c){
                    case 1: recordSale(); break;
                    case 2: totalsToday(); break;
                    case 3: totalsMonth(); break;
                }
            } catch (Exception e){
                Tui.toastError("Error: " + e.getMessage());
                pause();
            }
        }
    }

    private void recordSale(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Record Sale");
        String code = ConsoleIO.readLineOrCancel("Drug code");
        if (code == null) return;
        int qty = ConsoleIO.readIntOrCancel("Quantity");
        if (qty == Integer.MIN_VALUE) return;
        String cust = ConsoleIO.readLineOrCancel("Customer ID");
        if (cust == null) return;

        SaleTxn t = inv.recordSale(code, qty, cust);
        // Persist: append sale, save inventory
        saleLog.append(t);
        saver.saveDrugs();

        Tui.toastSuccess("Sale recorded. Total: " + TextPad.toFixed2(t.getTotal()));
        pause();
    }

    private void totalsToday(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Today's Totals");
        LocalDate today = LocalDate.now();
        SaleLogCsv.Totals t = saleLog.totalsForDay(today);
        showTotals(t, "Day " + today);
        pause();
    }

    private void totalsMonth(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Totals for Month (YYYY-MM)");
        String ym = ConsoleIO.readLineOrCancel("Year-Month");
        if (ym == null) return;
        SaleLogCsv.Totals t = saleLog.totalsForMonth(ym.trim());
        showTotals(t, "Month " + ym);
        pause();
    }

    private void showTotals(SaleLogCsv.Totals t, String label){
        ConsoleIO.println(label);
        ConsoleIO.println("- Transactions: " + t.count);
        ConsoleIO.println("- Units sold:  " + t.units);
        ConsoleIO.println("- Revenue:     " + TextPad.toFixed2(t.revenue));
    }

    private void pause(){ ConsoleIO.readLine("Press Enter to continue..."); }
}
