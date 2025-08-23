package atinka.ui;

import atinka.service.InventoryService;
import atinka.storage.SaleLogCsv;
import atinka.util.ConsoleIO;
import atinka.util.SimpleScreen;
import atinka.util.Tui;

import atinka.model.SaleTxn;
import atinka.dsa.Vec;

import java.time.LocalDate;

public final class SalesUI {
    private final InventoryService inv;
    private final SaleLogCsv saleLog;
    private final SaveHooks saver;

    public interface SaveHooks { void saveDrugs(); }

    public SalesUI(InventoryService inv, SaleLogCsv saleLog, SaveHooks saver){
        this.inv = inv; this.saleLog = saleLog; this.saver = saver;
    }

    public void show(){
        while (true){
            String[] algos = new String[]{ "HashMapOpen", "Vec", "Linear scan", "MergeSort (in reports)" };
            String[] body = new String[]{
                    " 1) Record sale                   — HashMapOpen lookup; O(1) stock adjust; CSV append",
                    " 2) Today's totals                — Linear scan aggregate",
                    " 3) Totals for month (YYYY-MM)    — Linear scan aggregate",
                    "",
                    " 0) Back"
            };
            SimpleScreen.render("Sales — Record & Totals", algos, body);

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
        SimpleScreen.render("Record Sale", new String[]{"HashMapOpen","O(1) stock adjust","CSV append"}, new String[0]);
        String code = ConsoleIO.readLineOrCancel("Drug code"); if (code == null) return;
        int qty = ConsoleIO.readIntOrCancel("Quantity"); if (qty == Integer.MIN_VALUE) return;
        String cust = ConsoleIO.readLineOrCancel("Customer ID"); if (cust == null) return;

        SaleTxn t = inv.recordSale(code, qty, cust);
        saleLog.append(t);
        saver.saveDrugs();

        Tui.toastSuccess("Sale recorded. Total: " + TextPad.toFixed2(t.getTotal()));
        pause();
    }

    private void totalsToday(){
        SimpleScreen.render("Today's Totals", new String[]{"Linear scan aggregate"}, new String[0]);
        SaleLogCsv.Totals t = saleLog.totalsForDay(LocalDate.now());
        showTotals(t, "Day " + LocalDate.now());
        pause();
    }

    private void totalsMonth(){
        SimpleScreen.render("Totals for Month (YYYY-MM)", new String[]{"Linear scan aggregate"}, new String[0]);
        String ym = ConsoleIO.readLineOrCancel("Year-Month"); if (ym == null) return;
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
