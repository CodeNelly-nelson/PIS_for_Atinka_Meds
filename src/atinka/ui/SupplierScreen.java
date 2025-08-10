package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Supplier;
import atinka.service.SupplierService;
import atinka.storage.SupplierCsvStore;
import atinka.util.ConsoleIO;

public final class SupplierScreen extends Screen {
    private final SupplierService svc; private final SupplierCsvStore store;
    public SupplierScreen(SupplierService svc, SupplierCsvStore store){ this.svc=svc; this.store=store; }

    @Override public void run() {
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Suppliers");
            ConsoleIO.println("1) Add supplier");
            ConsoleIO.println("2) List suppliers");
            ConsoleIO.println("3) Filter by location");
            ConsoleIO.println("4) Filter by turnaround <= days");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ",0,4);
            switch(c){
                case 1 -> add();
                case 2 -> list(svc.all());
                case 3 -> list(svc.filterByLocation(ConsoleIO.readLine("Location contains: ")));
                case 4 -> list(svc.filterByTurnaroundAtMost(ConsoleIO.readIntInRange("Days <= ",0,3650)));
                case 0 -> back=true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void add(){
        String name=ConsoleIO.readLine("Name: ");
        String loc=ConsoleIO.readLine("Location: ");
        int ta=ConsoleIO.readIntInRange("Turnaround days: ",0,365);
        String contact=ConsoleIO.readLine("Contact: ");
        Supplier s=svc.create(name,loc,ta,contact);
        store.saveAll(svc.all());
        ConsoleIO.println("Added supplier: "+s.getId());
    }

    private void list(Vec<Supplier> v){
        if(v.isEmpty()){ ConsoleIO.println("No suppliers."); return; }
        ConsoleIO.println(String.format("%-6s %-20s %-16s %6s %-15s","ID","NAME","LOCATION","TA","CONTACT"));
        for(int i=0;i<v.size();i++){
            Supplier s=v.get(i);
            ConsoleIO.println(String.format("%-6s %-20s %-16s %6d %-15s",
                    s.getId(), s.getName(), s.getLocation(), s.getTurnaroundDays(), s.getContact()));
        }
    }
}
