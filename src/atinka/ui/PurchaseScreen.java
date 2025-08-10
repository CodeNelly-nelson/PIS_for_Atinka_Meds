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
        String code=ConsoleIO.readLine("Drug code: ");
        int qty=ConsoleIO.readIntInRange("Quantity: ",0,Integer.MAX_VALUE);
        String buyer=ConsoleIO.readLine("Buyer ID: ");
        double unit=ConsoleIO.readPositiveDouble("Unit price for this purchase: ");
        PurchaseTxn t=inventory.recordPurchase(code, qty, buyer, unit);
        log.append(t);
        // persist stock changes using the drug service snapshot
        drugStore.saveAll(drugs.all());
        ConsoleIO.println("Recorded & saved purchase: "+t);

        // hint for alerts
        Vec<?> alerts = inventory.belowThreshold();
        if (alerts.size() > 0) ConsoleIO.println("Note: some items are below threshold. See Stock Monitor.");
    }

    private void latest5(){
        String code=ConsoleIO.readLine("Drug code: ");
        Vec<PurchaseTxn> sortedDesc = inventory.purchasesForDrugSortedByTime(code, false);
        if(sortedDesc==null || sortedDesc.size()==0){ ConsoleIO.println("No purchases for this drug."); return; }
        int limit = sortedDesc.size() < 5 ? sortedDesc.size() : 5;
        for(int i=0;i<limit;i++) ConsoleIO.println(sortedDesc.get(i).toString());
    }
}
