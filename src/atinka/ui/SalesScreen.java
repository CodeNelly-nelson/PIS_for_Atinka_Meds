package atinka.ui;

import atinka.model.SaleTxn;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.storage.DrugCsvStore;
import atinka.storage.SaleLogCsv;
import atinka.util.ConsoleIO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public final class SalesScreen extends Screen {
    private final DrugService drugs;
    private final InventoryService inventory;
    private final SaleLogCsv log;
    private final DrugCsvStore drugStore;

    public SalesScreen(DrugService drugs, InventoryService inv, SaleLogCsv log, DrugCsvStore drugStore){
        this.drugs = drugs;
        this.inventory=inv;
        this.log=log;
        this.drugStore=drugStore;
    }

    @Override public void run() {
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Sales");
            ConsoleIO.println("1) Record sale");
            ConsoleIO.println("2) Sales between dates (from log)");
            ConsoleIO.println("3) Daily sales & revenue");
            ConsoleIO.println("4) Monthly sales & revenue");
            ConsoleIO.println("0) Back\n");
            int c=ConsoleIO.readIntInRange("Choose: ",0,4);
            switch(c){
                case 1 -> add();
                case 2 -> between();
                case 3 -> daily();
                case 4 -> monthly();
                case 0 -> back=true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void add(){
        try {
            String code=ConsoleIO.readLineOrCancel("Drug code");
            if (code==null) { ConsoleIO.println("Cancelled."); return; }

            int qty=ConsoleIO.readIntOrCancel("Quantity");
            if (qty==Integer.MIN_VALUE) { ConsoleIO.println("Cancelled."); return; }

            String cust=ConsoleIO.readLineOrCancel("Customer ID (optional)");
            if (cust==null) cust = "";

            SaleTxn t=inventory.recordSale(code, qty, cust);
            log.append(t);
            drugStore.saveAll(drugs.all());
            ConsoleIO.println("Recorded & saved sale: "+t);

            if (inventory.belowThreshold().size() > 0)
                ConsoleIO.println("Note: some items are below threshold. See Stock Monitor.");
        } catch (IllegalStateException ise) {
            // handles "Expired" and "Insufficient stock"
            ConsoleIO.println("Cannot record sale: " + ise.getMessage());
        } catch (Exception ex) {
            ConsoleIO.println("Error: " + ex.getMessage());
        }
    }

    private void between(){
        String from=ConsoleIO.readLineOrCancel("From (YYYY-MM-DD)");
        if (from==null) { ConsoleIO.println("Cancelled."); return; }
        String to=ConsoleIO.readLineOrCancel("To   (YYYY-MM-DD)");
        if (to==null) { ConsoleIO.println("Cancelled."); return; }

        try {
            LocalDateTime a= LocalDate.parse(from).atStartOfDay();
            LocalDateTime b= LocalDate.parse(to).atTime(23,59,59);
            SaleTxn[] all = log.readAll();
            double total=0.0; int count=0;
            for(int i=0;i<all.length;i++){
                SaleTxn s=all[i];
                if(!s.getTimestamp().isBefore(a) && !s.getTimestamp().isAfter(b)){ count++; total+=s.getTotal(); }
            }
            ConsoleIO.println("Sales count: "+count);
            ConsoleIO.println(String.format("Total value: %.2f", total));
        } catch (Exception ex) {
            ConsoleIO.println("Error: " + ex.getMessage());
        }
    }

    /** Accounting: summary for a specific date. */
    private void daily() {
        String ds = ConsoleIO.readLineOrCancel("Date (YYYY-MM-DD)");
        if (ds==null) { ConsoleIO.println("Cancelled."); return; }
        try {
            LocalDate d = LocalDate.parse(ds);
            LocalDateTime a = d.atStartOfDay();
            LocalDateTime b = d.atTime(23,59,59);
            SaleTxn[] all = log.readAll();
            double total=0.0; int count=0;
            for (int i=0;i<all.length;i++){
                SaleTxn s=all[i];
                if(!s.getTimestamp().isBefore(a) && !s.getTimestamp().isAfter(b)){ count++; total+=s.getTotal(); }
            }
            ConsoleIO.println("Date: " + ds);
            ConsoleIO.println("Sales count: " + count);
            ConsoleIO.println(String.format("Revenue: %.2f", total));
        } catch (Exception ex) {
            ConsoleIO.println("Error: " + ex.getMessage());
        }
    }

    /** Accounting: summary for a YYYY-MM month. */
    private void monthly() {
        String ms = ConsoleIO.readLineOrCancel("Month (YYYY-MM)");
        if (ms==null) { ConsoleIO.println("Cancelled."); return; }
        try {
            YearMonth ym = YearMonth.parse(ms);
            LocalDateTime a = ym.atDay(1).atStartOfDay();
            LocalDateTime b = ym.atEndOfMonth().atTime(23,59,59);
            SaleTxn[] all = log.readAll();
            double total=0.0; int count=0;
            for (int i=0;i<all.length;i++){
                SaleTxn s=all[i];
                if(!s.getTimestamp().isBefore(a) && !s.getTimestamp().isAfter(b)){ count++; total+=s.getTotal(); }
            }
            ConsoleIO.println("Month: " + ms);
            ConsoleIO.println("Sales count: " + count);
            ConsoleIO.println(String.format("Revenue: %.2f", total));
        } catch (Exception ex) {
            ConsoleIO.println("Error: " + ex.getMessage());
        }
    }
}
