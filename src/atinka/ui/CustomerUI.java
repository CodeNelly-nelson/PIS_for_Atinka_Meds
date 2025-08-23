package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Customer;
import atinka.service.CustomerService;
import atinka.util.ConsoleIO;
import atinka.util.SimpleScreen;
import atinka.util.Tui;

public final class CustomerUI {
    private final CustomerService svc;
    private final SaveHooks saver;

    public interface SaveHooks { void saveCustomers(); }

    public CustomerUI(CustomerService svc, SaveHooks saver){
        this.svc = svc; this.saver = saver;
    }

    public void show(){
        while (true){
            String[] algos = new String[]{ "Vec", "HashMapOpen", "Linear scan" };
            String[] body = new String[]{
                    " 1) List all                  — Linear scan over Vec",
                    " 2) Add new                   — HashMapOpen uniqueness",
                    " 3) Update                    — HashMapOpen",
                    " 4) Remove                    — Linear scan + HashMapOpen",
                    "",
                    " 0) Back"
            };
            SimpleScreen.render("Customers — Manage", algos, body);

            int c = ConsoleIO.readIntInRange("Choose: ", 0, 4);
            if (c == 0) return;
            try {
                switch (c){
                    case 1: listAll(); break;
                    case 2: addNew(); break;
                    case 3: update(); break;
                    case 4: remove(); break;
                }
            } catch (Exception e){
                Tui.toastError("Error: " + e.getMessage());
                pause();
            }
        }
    }

    private void listAll(){
        SimpleScreen.render("Customers — All", new String[]{"Linear scan","Vec"}, new String[0]);
        Vec<Customer> v = svc.all();
        ConsoleIO.println(TextPad.padRight("ID", 8) + "  " +
                TextPad.padRight("NAME", 26) + "  " +
                TextPad.padRight("CONTACT", 22));
        for (int i=0;i<v.size();i++){
            Customer c = v.get(i);
            ConsoleIO.println(TextPad.padRight(c.getId(),8) + "  " +
                    TextPad.padRight(limit(c.getName(),26),26) + "  " +
                    TextPad.padRight(limit(c.getContact(),22),22));
        }
        pause();
    }

    private void addNew(){
        SimpleScreen.render("Add Customer", new String[]{"HashMapOpen index"}, new String[0]);
        String id = ConsoleIO.readLineOrCancel("ID"); if (id == null) return;
        if (svc.getById(id) != null){ Tui.toastWarn("ID exists."); pause(); return; }
        String name = ConsoleIO.readLineOrCancel("Name"); if (name == null) return;
        String contact = ConsoleIO.readLineOrCancel("Contact"); if (contact == null) return;

        boolean ok = svc.add(new Customer(id.trim(), name.trim(), contact.trim()));
        if (ok){ saver.saveCustomers(); Tui.toastSuccess("Added."); } else Tui.toastWarn("Failed.");
        pause();
    }

    private void update(){
        SimpleScreen.render("Update Customer", new String[]{"HashMapOpen index"}, new String[0]);
        String id = ConsoleIO.readLineOrCancel("ID"); if (id == null) return;
        Customer c = svc.getById(id);
        if (c == null){ Tui.toastWarn("Not found."); pause(); return; }
        String name = ConsoleIO.readLine("New name (empty=keep): ");
        String contact = ConsoleIO.readLine("New contact (empty=keep): ");
        boolean ok = svc.update(id, emptyAsNull(name), emptyAsNull(contact));
        if (ok){ saver.saveCustomers(); Tui.toastSuccess("Updated."); } else Tui.toastWarn("Failed.");
        pause();
    }

    private void remove(){
        SimpleScreen.render("Remove Customer", new String[]{"Linear scan","HashMapOpen index"}, new String[0]);
        String id = ConsoleIO.readLineOrCancel("ID"); if (id == null) return;
        boolean ok = svc.remove(id);
        if (ok){ saver.saveCustomers(); Tui.toastSuccess("Removed."); } else Tui.toastWarn("Not found.");
        pause();
    }

    private String emptyAsNull(String s){ return (s==null || s.trim().length()==0) ? null : s.trim(); }
    private String limit(String s, int n){
        if (s==null) return "";
        if (s.length()<=n) return s;
        if (n<=1) return s.substring(0,n);
        return s.substring(0,n-1) + "…";
    }
    private void pause(){ ConsoleIO.readLine("Press Enter to continue..."); }
}
