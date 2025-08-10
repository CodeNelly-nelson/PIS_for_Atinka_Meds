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
            ConsoleIO.println("0) Back\n");
            int c=ConsoleIO.readIntInRange("Choose: ",0,2);
            switch(c){
                case 1 -> add();
                case 2 -> list(svc.all());
                case 0 -> back=true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void add(){
        String name=ConsoleIO.readLine("Name: ");
        String contact=ConsoleIO.readLine("Contact: ");
        Customer cu=svc.create(name, contact);
        store.saveAll(svc.all());
        ConsoleIO.println("Added customer: "+cu.getId());
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
