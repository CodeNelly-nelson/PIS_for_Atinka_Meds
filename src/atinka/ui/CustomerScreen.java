package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Customer;
import atinka.service.CustomerService;
import atinka.storage.CustomerCsvStore;
import atinka.util.ConsoleIO;

public final class CustomerScreen extends Screen {
    private final CustomerService svc; private final CustomerCsvStore store;
    public CustomerScreen(CustomerService svc, CustomerCsvStore store){ this.svc=svc; this.store=store; }

    @Override public void run() {
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Customers");
            ConsoleIO.println("1) Add customer");
            ConsoleIO.println("2) List customers");
            ConsoleIO.println("3) Update customer");
            ConsoleIO.println("4) Remove customer");
            ConsoleIO.println("0) Back\n");
            int c=ConsoleIO.readIntInRange("Choose: ",0,4);
            switch(c){
                case 1 -> add();
                case 2 -> list(svc.all());
                case 3 -> update();
                case 4 -> remove();
                case 0 -> back=true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void add(){
        String name=ConsoleIO.readLineOrCancel("Name");
        if (name==null) { ConsoleIO.println("Cancelled."); return; }
        String contact=ConsoleIO.readLineOrCancel("Contact");
        if (contact==null) { ConsoleIO.println("Cancelled."); return; }
        Customer cu=svc.create(name, contact);
        store.saveAll(svc.all());
        ConsoleIO.println("Added customer: "+cu.getId());
    }

    private void update() {
        String id = ConsoleIO.readLineOrCancel("Customer ID");
        if (id==null) { ConsoleIO.println("Cancelled."); return; }
        Customer existing = svc.get(id);
        if (existing == null) { ConsoleIO.println("Not found."); return; }
        String name = ConsoleIO.readLine("Name ["+existing.getName()+"]: ");
        if (!name.isEmpty()) existing.setName(name);
        String contact = ConsoleIO.readLine("Contact ["+existing.getContact()+"]: ");
        if (!contact.isEmpty()) existing.setContact(contact);
        if (svc.update(existing)) {
            store.saveAll(svc.all());
            ConsoleIO.println("Updated & saved.");
        } else {
            ConsoleIO.println("Update failed.");
        }
    }

    private void remove() {
        String id = ConsoleIO.readLineOrCancel("Customer ID");
        if (id==null) { ConsoleIO.println("Cancelled."); return; }
        if (svc.remove(id)) {
            store.saveAll(svc.all());
            ConsoleIO.println("Removed & saved.");
        } else {
            ConsoleIO.println("Not found; nothing removed.");
        }
    }

    private void list(Vec<Customer> v){
        if(v.isEmpty()){ ConsoleIO.println("No customers."); return; }
        ConsoleIO.println(String.format("%-6s %-24s %-18s","ID","NAME","CONTACT"));
        for(int i=0;i<v.size();i++){
            Customer c=v.get(i);
            ConsoleIO.println(String.format("%-6s %-24s %-18s", c.getId(), c.getName(), c.getContact()));
        }
    }
}
