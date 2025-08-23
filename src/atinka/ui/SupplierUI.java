package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Supplier;
import atinka.service.SupplierService;
import atinka.util.ConsoleIO;
import atinka.util.Tui;

public final class SupplierUI {
    private final SupplierService svc;
    private final SaveHooks saver;

    public interface SaveHooks {
        void saveSuppliers();
    }

    public SupplierUI(SupplierService svc, SaveHooks saver){
        this.svc = svc; this.saver = saver;
    }

    public void show(){
        while (true){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Suppliers");
            ConsoleIO.println("1) List all (by name)");
            ConsoleIO.println("2) Add new");
            ConsoleIO.println("3) Update");
            ConsoleIO.println("4) Remove");
            ConsoleIO.println("5) Filter by location contains");
            ConsoleIO.println("6) Filter by turnaround days (<=)");
            ConsoleIO.println("0) Back");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 6);
            if (c == 0) return;
            try {
                switch (c){
                    case 1: listAll(); break;
                    case 2: addNew(); break;
                    case 3: update(); break;
                    case 4: remove(); break;
                    case 5: filterLocation(); break;
                    case 6: filterTurnaround(); break;
                }
            } catch (Exception e){
                Tui.toastError("Error: " + e.getMessage());
                pause();
            }
        }
    }

    private void listAll(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Suppliers — All (by name)");
        Vec<Supplier> v = svc.sortedByName();
        ConsoleIO.println(TextPad.padRight("ID", 8) + "  " +
                TextPad.padRight("NAME", 26) + "  " +
                TextPad.padRight("CONTACT", 18) + "  " +
                TextPad.padRight("LOCATION", 12) + "  " +
                TextPad.padLeft("TA", 3));
        for (int i=0;i<v.size();i++){
            Supplier s = v.get(i);
            ConsoleIO.println(TextPad.padRight(s.getId(),8) + "  " +
                    TextPad.padRight(limit(s.getName(),26),26) + "  " +
                    TextPad.padRight(limit(s.getContact(),18),18) + "  " +
                    TextPad.padRight(limit(s.getLocation(),12),12) + "  " +
                    TextPad.padLeft(String.valueOf(s.getTurnaroundDays()),3));
        }
        pause();
    }

    private void addNew(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Add Supplier");
        String id = ConsoleIO.readLineOrCancel("ID");
        if (id == null) return;
        if (svc.getById(id) != null){ Tui.toastWarn("ID exists."); pause(); return; }
        String name = ConsoleIO.readLineOrCancel("Name");
        if (name == null) return;
        String contact = ConsoleIO.readLineOrCancel("Contact");
        if (contact == null) return;
        String location = ConsoleIO.readLineOrCancel("Location");
        if (location == null) return;
        int ta = ConsoleIO.readIntOrCancel("Turnaround days");
        if (ta == Integer.MIN_VALUE) return;

        boolean ok = svc.add(new Supplier(id.trim(), name.trim(), contact.trim(), location.trim(), ta));
        if (ok){ saver.saveSuppliers(); Tui.toastSuccess("Added."); } else Tui.toastWarn("Failed.");
        pause();
    }

    private void update(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Update Supplier");
        String id = ConsoleIO.readLineOrCancel("ID");
        if (id == null) return;
        Supplier s = svc.getById(id);
        if (s == null){ Tui.toastWarn("Not found."); pause(); return; }

        String name = ConsoleIO.readLine("New name (empty=keep): ");
        String contact = ConsoleIO.readLine("New contact (empty=keep): ");
        String location = ConsoleIO.readLine("New location (empty=keep): ");
        String taStr = ConsoleIO.readLine("New turnaround days (empty=keep): ");
        Integer ta = null;
        if (taStr != null && taStr.trim().length()>0){
            try { ta = Integer.valueOf(Integer.parseInt(taStr.trim())); } catch(Exception ignored){}
        }

        boolean ok = svc.update(id, emptyAsNull(name), emptyAsNull(contact), emptyAsNull(location), ta);
        if (ok){ saver.saveSuppliers(); Tui.toastSuccess("Updated."); } else Tui.toastWarn("Failed.");
        pause();
    }

    private void remove(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Remove Supplier");
        String id = ConsoleIO.readLineOrCancel("ID");
        if (id == null) return;
        boolean ok = svc.remove(id);
        if (ok){ saver.saveSuppliers(); Tui.toastSuccess("Removed."); } else Tui.toastWarn("Not found.");
        pause();
    }

    private void filterLocation(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Filter — Location contains");
        String term = ConsoleIO.readLineOrCancel("Term");
        if (term == null) return;
        Vec<Supplier> v = svc.filterByLocationContains(term);
        if (v.size()==0){ Tui.toastInfo("No matches."); pause(); return; }
        for (int i=0;i<v.size();i++){
            Supplier s = v.get(i);
            ConsoleIO.println(s.getId() + "  " + s.getName() + "  " + s.getLocation());
        }
        pause();
    }

    private void filterTurnaround(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Filter — Turnaround <= days");
        int d = ConsoleIO.readIntOrCancel("Max days");
        if (d == Integer.MIN_VALUE) return;
        Vec<Supplier> v = svc.filterByTurnaroundAtMost(d);
        if (v.size()==0){ Tui.toastInfo("No matches."); pause(); return; }
        for (int i=0;i<v.size();i++){
            Supplier s = v.get(i);
            ConsoleIO.println(s.getId() + "  " + s.getName() + "  " + s.getTurnaroundDays() + "d");
        }
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
