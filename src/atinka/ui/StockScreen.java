package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.service.InventoryService;
import atinka.util.ConsoleIO;
import atinka.util.Tui;
import atinka.util.Ansi;

public final class StockScreen extends Screen {
    private final InventoryService inventory;

    public StockScreen(InventoryService inventory){
        this.inventory = inventory;
    }

    @Override public void run(){
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Stock Monitor");
            System.out.println("1) Show low-stock (Top N)");
            System.out.println("2) List all below threshold");
            System.out.println("0) Back\n");

            int c = ConsoleIO.readIntInRange("Choose", 0, 2);
            switch (c){
                case 1 -> topN();
                case 2 -> belowThreshold();
                case 0 -> back = true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void topN(){
        int n = ConsoleIO.readIntOrCancel("N (how many)");
        if (n == Integer.MIN_VALUE) { Tui.toastInfo("Cancelled."); return; }
        Vec<Drug> v = inventory.lowStockTopN(n);
        if (v == null || v.size() == 0){ Tui.toastInfo("No items."); return; }

        Tui.breadcrumb("Stock › Top " + n + " Lowest");
        Tui.table(
                new String[]{"CODE","NAME","STOCK","THRESH"},
                new int[]{12,30,8,8},
                new Tui.RowProvider(){
                    int i=0;
                    @Override public String[] nextRow(int idx){
                        if (i >= v.size()) return null;
                        Drug d = v.get(i++);
                        return new String[]{ d.getCode(), d.getName(),
                                String.valueOf(d.getStock()),
                                String.valueOf(d.getReorderThreshold()) };
                    }
                    @Override public String rowColor(int idx) {
                        Drug d = v.get(idx);
                        if (d.getStock() <= 0) return Ansi.FG_RED;
                        if (d.getStock() < d.getReorderThreshold()) return Ansi.FG_YELLOW;
                        return null;
                    }
                }
        );
    }

    private void belowThreshold(){
        Vec<Drug> v = inventory.belowThreshold();
        if (v == null || v.size() == 0){ Tui.toastInfo("All items are at healthy levels."); return; }
        Tui.breadcrumb("Stock › Below Threshold");
        Tui.table(
                new String[]{"CODE","NAME","STOCK","THRESH"},
                new int[]{12,30,8,8},
                new Tui.RowProvider(){
                    int i=0;
                    @Override public String[] nextRow(int idx){
                        if (i >= v.size()) return null;
                        Drug d = v.get(i++);
                        return new String[]{ d.getCode(), d.getName(),
                                String.valueOf(d.getStock()),
                                String.valueOf(d.getReorderThreshold()) };
                    }
                    @Override public String rowColor(int idx) { return Ansi.FG_YELLOW; }
                }
        );
    }
}
