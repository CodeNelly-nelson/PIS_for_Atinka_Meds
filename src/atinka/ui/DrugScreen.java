package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.service.DrugService;
import atinka.service.InventoryService;
import atinka.storage.DrugCsvStore;
import atinka.util.ConsoleIO;

import java.time.LocalDate;

public final class DrugScreen extends Screen {
    private final DrugService drugs;
    private final InventoryService inventory;
    private final DrugCsvStore store;

    public DrugScreen(DrugService drugs, InventoryService inventory, DrugCsvStore store){
        this.drugs=drugs; this.inventory=inventory; this.store=store;
    }

    @Override public void run(){
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Drugs");
            ConsoleIO.println("1) Add drug");
            ConsoleIO.println("2) List drugs");
            ConsoleIO.println("3) Update drug");
            ConsoleIO.println("4) Remove drug");
            ConsoleIO.println("5) Search by code (binary)");
            ConsoleIO.println("6) Search name contains (linear)");
            ConsoleIO.println("7) Sort by name (merge)");
            ConsoleIO.println("8) Sort by price (merge)");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ",0,8);
            switch(c){
                case 1 -> add();
                case 2 -> list(drugs.all());
                case 3 -> update();
                case 4 -> remove();
                case 5 -> searchCode();
                case 6 -> searchName();
                case 7 -> list(drugs.sortedByName());
                case 8 -> list(drugs.sortedByPrice());
                case 0 -> back=true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void add(){
        try {
            String code = ConsoleIO.readLineOrCancel("Code");
            if (code==null) { ConsoleIO.println("Cancelled."); return; }
            if (drugs.getByCode(code)!=null){ ConsoleIO.println("Code already exists."); return; }

            String name = ConsoleIO.readLineOrCancel("Name");
            if (name==null) { ConsoleIO.println("Cancelled."); return; }

            double price = ConsoleIO.readPositiveDoubleOrCancel("Price");
            if (price==Double.NEGATIVE_INFINITY) { ConsoleIO.println("Cancelled."); return; }

            int stock = ConsoleIO.readIntOrCancel("Initial stock");
            if (stock==Integer.MIN_VALUE) { ConsoleIO.println("Cancelled."); return; }
            if (stock<0){ ConsoleIO.println("Stock cannot be negative."); return; }

            String exp = ConsoleIO.readLineOrCancel("Expiry (YYYY-MM-DD)");
            if (exp==null) { ConsoleIO.println("Cancelled."); return; }
            LocalDate expiry;
            try { expiry = LocalDate.parse(exp); } catch (Exception e){ ConsoleIO.println("Invalid date."); return; }

            int thresh = ConsoleIO.readIntOrCancel("Reorder threshold");
            if (thresh==Integer.MIN_VALUE) { ConsoleIO.println("Cancelled."); return; }
            if (thresh<0){ ConsoleIO.println("Threshold cannot be negative."); return; }

            Drug d = new Drug(code, name, price, stock, expiry, thresh);
            if (drugs.add(d)) { store.saveAll(drugs.all()); ConsoleIO.println("Added."); }
            else ConsoleIO.println("Failed to add (duplicate?)");
        } catch (Exception ex) {
            ConsoleIO.println("Error: "+ex.getMessage());
        }
    }

    private void update(){
        String code = ConsoleIO.readLineOrCancel("Code");
        if (code==null) { ConsoleIO.println("Cancelled."); return; }
        Drug d = drugs.getByCode(code);
        if (d==null){ ConsoleIO.println("Not found."); return; }

        String name = ConsoleIO.readLineOrCancel("Name ["+d.getName()+"]");
        if (name==null){ ConsoleIO.println("Cancelled."); return; }
        if (!name.isEmpty()) d.setName(name);

        String p = ConsoleIO.readLineOrCancel("Price ["+d.getPrice()+"]");
        if (p==null){ ConsoleIO.println("Cancelled."); return; }
        if (!p.isEmpty()){
            try { double v=Double.parseDouble(p); if (v<0){ ConsoleIO.println("Price cannot be negative."); return; } d.setPrice(v); }
            catch(Exception e){ ConsoleIO.println("Invalid price."); return; }
        }

        String s = ConsoleIO.readLineOrCancel("Stock ["+d.getStock()+"]");
        if (s==null){ ConsoleIO.println("Cancelled."); return; }
        if (!s.isEmpty()){
            try { int v=Integer.parseInt(s); if (v<0){ ConsoleIO.println("Stock cannot be negative."); return; } d.setStock(v); }
            catch(Exception e){ ConsoleIO.println("Invalid stock."); return; }
        }

        String ex = ConsoleIO.readLineOrCancel("Expiry ["+d.getExpiry()+"]");
        if (ex==null){ ConsoleIO.println("Cancelled."); return; }
        if (!ex.isEmpty()){
            try { d.setExpiry(LocalDate.parse(ex)); } catch(Exception e){ ConsoleIO.println("Invalid date."); return; }
        }

        String th = ConsoleIO.readLineOrCancel("Reorder threshold ["+d.getReorderThreshold()+"]");
        if (th==null){ ConsoleIO.println("Cancelled."); return; }
        if (!th.isEmpty()){
            try { int v=Integer.parseInt(th); if (v<0){ ConsoleIO.println("Threshold cannot be negative."); return; } d.setReorderThreshold(v); }
            catch(Exception e){ ConsoleIO.println("Invalid threshold."); return; }
        }

        if (drugs.update(d)) { store.saveAll(drugs.all()); ConsoleIO.println("Updated."); }
        else ConsoleIO.println("Update failed.");
    }

    private void remove(){
        String code = ConsoleIO.readLineOrCancel("Code");
        if (code==null){ ConsoleIO.println("Cancelled."); return; }
        if (drugs.remove(code)) { store.saveAll(drugs.all()); ConsoleIO.println("Removed."); }
        else ConsoleIO.println("Not found.");
    }

    private void searchCode(){
        String code = ConsoleIO.readLineOrCancel("Code");
        if (code==null){ ConsoleIO.println("Cancelled."); return; }
        Drug d = drugs.getByCode(code);
        if (d==null) ConsoleIO.println("Not found.");
        else list(one(d));
    }

    private void searchName(){
        String q = ConsoleIO.readLineOrCancel("Name contains");
        if (q==null){ ConsoleIO.println("Cancelled."); return; }
        list(drugs.searchNameContains(q));
    }

    private void list(Vec<Drug> v){
        if (v==null || v.size()==0){ ConsoleIO.println("No items."); return; }
        ConsoleIO.println(String.format("%-12s %-30s %8s %8s %10s %8s","CODE","NAME","PRICE","STOCK","EXPIRY","THRESH"));
        for (int i=0;i<v.size();i++){
            Drug d = v.get(i);
            ConsoleIO.println(String.format("%-12s %-30s %8.2f %8d %10s %8d",
                    d.getCode(), d.getName(), d.getPrice(), d.getStock(),
                    d.getExpiry()==null?"":d.getExpiry().toString(), d.getReorderThreshold()));
        }
    }
    private Vec<Drug> one(Drug d){ Vec<Drug> r=new Vec<>(); r.add(d); return r; }
}
