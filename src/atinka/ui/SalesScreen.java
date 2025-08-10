package atinka.ui;

import atinka.model.SaleTxn;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.storage.DrugCsvStore;
import atinka.storage.SaleLogCsv;
import atinka.util.ConsoleIO;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
            ConsoleIO.println("0) Back\n");
            int c=ConsoleIO.readIntInRange("Choose: ",0,2);
            switch(c){
                case 1 -> add();
                case 2 -> between();
                case 0 -> back=true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void add(){
        String code=ConsoleIO.readLine("Drug code: ");
        int qty=ConsoleIO.readIntInRange("Quantity: ",1,Integer.MAX_VALUE);
        String cust=ConsoleIO.readLine("Customer ID (optional): ");
        SaleTxn t=inventory.recordSale(code, qty, cust);
        log.append(t);
        // persist stock changes using the drug service snapshot
        drugStore.saveAll(drugs.all());
        ConsoleIO.println("Recorded & saved sale: "+t);

        // quick heads up if anything is below threshold
        if (inventory.belowThreshold().size() > 0)
            ConsoleIO.println("Note: some items are below threshold. See Stock Monitor.");
    }

    private void between(){
        String from=ConsoleIO.readLine("From (YYYY-MM-DD): ");
        String to=ConsoleIO.readLine("To   (YYYY-MM-DD): ");
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
    }
}
