package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.service.InventoryService;
import atinka.util.ConsoleIO;

public final class StockScreen extends Screen {
    private final InventoryService inventory;
    public StockScreen(InventoryService inv){ this.inventory=inv; }

    @Override public void run() {
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Stock Monitor");
            ConsoleIO.println("1) Show low-stock (Top N)");
            ConsoleIO.println("2) Show items below threshold");
            ConsoleIO.println("0) Back\n");
            int c=ConsoleIO.readIntInRange("Choose: ",0,2);
            switch(c){
                case 1 -> topN();
                case 2 -> belowThreshold();
                case 0 -> back=true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void topN(){
        int n=ConsoleIO.readIntInRange("How many items? ",1,Integer.MAX_VALUE);
        Vec<Drug> list=inventory.lowStockTopN(n);
        render(list, false);
    }

    private void belowThreshold(){
        Vec<Drug> list=inventory.belowThreshold();
        render(list, true);
    }

    private void render(Vec<Drug> list, boolean withThresh){
        if(list==null || list.size()==0){ ConsoleIO.println("No data."); return; }
        if(withThresh){
            ConsoleIO.println(String.format("%-12s %-28s %8s %8s %8s","CODE","NAME","STOCK","THRESH","PRICE"));
            for(int i=0;i<list.size();i++){
                Drug d=list.get(i);
                ConsoleIO.println(String.format("%-12s %-28s %8d %8d %8.2f",
                        d.getCode(), d.getName(), d.getStock(), d.getReorderThreshold(), d.getPrice()));
            }
        }else{
            ConsoleIO.println(String.format("%-12s %-28s %8s %8s","CODE","NAME","PRICE","STOCK"));
            for(int i=0;i<list.size();i++){
                Drug d=list.get(i);
                ConsoleIO.println(String.format("%-12s %-28s %8.2f %8d",
                        d.getCode(), d.getName(), d.getPrice(), d.getStock()));
            }
        }
    }
}
