package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.PurchaseTxn;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.storage.DrugCsvStore;
import atinka.storage.PurchaseLogCsv;
import atinka.util.ConsoleIO;

public final class PurchaseScreen extends Screen {
    private final DrugService drugs;
    private final InventoryService inventory;
    private final PurchaseLogCsv log;
    private final DrugCsvStore drugStore;

    public PurchaseScreen(DrugService drugs, InventoryService inv, PurchaseLogCsv log, DrugCsvStore drugStore){
        this.drugs = drugs;
        this.inventory = inv;
        this.log = log;
        this.drugStore = drugStore;
    }

    @Override public void run() {
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Purchases");
            ConsoleIO.println("1) Record purchase");
            ConsoleIO.println("2) Latest 5 purchases (sorted by time)");
            ConsoleIO.println("0) Back\n");
            int c=ConsoleIO.readIntInRange("Choose: ",0,2);
            switch(c){
                case 1 -> add();
                case 2 -> latest5();
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

            String buyer=ConsoleIO.readLineOrCancel("Buyer ID");
            if (buyer==null) { ConsoleIO.println("Cancelled."); return; }

            double unit=ConsoleIO.readPositiveDoubleOrCancel("Unit price for this purchase");
            if (unit==Double.NEGATIVE_INFINITY) { ConsoleIO.println("Cancelled."); return; }

            PurchaseTxn t=inventory.recordPurchase(code, qty, buyer, unit);
            log.append(t);
            drugStore.saveAll(drugs.all());
            ConsoleIO.println("Recorded & saved purchase: "+t);
            if (inventory.belowThreshold().size() > 0)
                ConsoleIO.println("Note: some items are below threshold. See Stock Monitor.");
        } catch (Exception ex) {
            ConsoleIO.println("Error: " + ex.getMessage());
        }
    }

    private void latest5(){
        String code=ConsoleIO.readLineOrCancel("Drug code");
        if (code==null) { ConsoleIO.println("Cancelled."); return; }
        Vec<PurchaseTxn> sortedDesc = inventory.purchasesForDrugSortedByTime(code, false);
        if(sortedDesc==null || sortedDesc.size()==0){ ConsoleIO.println("No purchases for this drug."); return; }

        ConsoleIO.println(String.format("%-12s %-10s %-22s %-10s %-10s",
                "TXN_ID","QTY","TIMESTAMP","BUYER_ID","TOTAL"));
        int limit = sortedDesc.size() < 5 ? sortedDesc.size() : 5;
        for(int i=0;i<limit;i++) {
            PurchaseTxn p = sortedDesc.get(i);
            ConsoleIO.println(String.format("%-12s %-10d %-22s %-10s %-10.2f",
                    p.getId(), p.getQty(), p.getTimestamp()==null?"":p.getTimestamp().toString(), p.getBuyerId(), p.getTotal()));
        }
    }
}
