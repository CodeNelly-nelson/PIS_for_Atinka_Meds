package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Supplier;
import atinka.service.SupplierService;
import atinka.storage.SupplierCsvStore;
import atinka.util.ConsoleIO;
import atinka.util.SafeParse;

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
            ConsoleIO.println("5) Update supplier");
            ConsoleIO.println("6) Remove supplier");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ",0,6);
            switch(c){
                case 1 -> add();
                case 2 -> list(svc.all());
                case 3 -> filterLocation();
                case 4 -> filterTurnaround();
                case 5 -> update();
                case 6 -> remove();
                case 0 -> back=true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void add(){
        try {
            String name=ConsoleIO.readLineOrCancel("Name");
            if (name==null) { ConsoleIO.println("Cancelled."); return; }
            String loc=ConsoleIO.readLineOrCancel("Location");
            if (loc==null) { ConsoleIO.println("Cancelled."); return; }
            int ta=ConsoleIO.readIntOrCancel("Turnaround days");
            if (ta==Integer.MIN_VALUE) { ConsoleIO.println("Cancelled."); return; }
            if (ta < 0) { ConsoleIO.println("Days must be >= 0"); return; }
            String contact=ConsoleIO.readLineOrCancel("Contact");
            if (contact==null) { ConsoleIO.println("Cancelled."); return; }

            Supplier s=svc.create(name,loc,ta,contact);
            store.saveAll(svc.all());
            ConsoleIO.println("Added supplier: "+s.getId());
        } catch (Exception ex) {
            ConsoleIO.println("Error: " + ex.getMessage());
        }
    }

    private void update() {
        String id = ConsoleIO.readLineOrCancel("Supplier ID");
        if (id==null) { ConsoleIO.println("Cancelled."); return; }
        Supplier existing = svc.get(id);
        if (existing == null) { ConsoleIO.println("Not found."); return; }

        ConsoleIO.println("Enter new values or leave empty to keep current. Enter 0 to cancel.");

        String name = ConsoleIO.readLineOrCancel("Name ["+existing.getName()+"]");
        if (name==null) { ConsoleIO.println("Cancelled."); return; }
        if (!name.isEmpty()) existing.setName(name);

        String location = ConsoleIO.readLineOrCancel("Location ["+existing.getLocation()+"]");
        if (location==null) { ConsoleIO.println("Cancelled."); return; }
        if (!location.isEmpty()) existing.setLocation(location);

        String ta = ConsoleIO.readLineOrCancel("Turnaround days ["+existing.getTurnaroundDays()+"]");
        if (ta==null) { ConsoleIO.println("Cancelled."); return; }
        if (!ta.isEmpty()) {
            int v = SafeParse.toInt(ta, Integer.MIN_VALUE);
            if (v < 0) { ConsoleIO.println("Invalid days."); return; }
            existing.setTurnaroundDays(v);
        }

        String contact = ConsoleIO.readLineOrCancel("Contact ["+existing.getContact()+"]");
        if (contact==null) { ConsoleIO.println("Cancelled."); return; }
        if (!contact.isEmpty()) existing.setContact(contact);

        if (svc.update(existing)) {
            store.saveAll(svc.all());
            ConsoleIO.println("Updated & saved.");
        } else {
            ConsoleIO.println("Update failed.");
        }
    }

    private void remove() {
        String id = ConsoleIO.readLineOrCancel("Supplier ID");
        if (id==null) { ConsoleIO.println("Cancelled."); return; }
        if (svc.remove(id)) {
            store.saveAll(svc.all());
            ConsoleIO.println("Removed & saved.");
        } else {
            ConsoleIO.println("Not found; nothing removed.");
        }
    }

    private void filterLocation(){
        String q = ConsoleIO.readLineOrCancel("Location contains");
        if (q==null) { ConsoleIO.println("Cancelled."); return; }
        list(svc.filterByLocation(q));
    }

    private void filterTurnaround(){
        int days = ConsoleIO.readIntOrCancel("Days <=");
        if (days == Integer.MIN_VALUE) { ConsoleIO.println("Cancelled."); return; }
        list(svc.filterByTurnaroundAtMost(days));
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
