package atinka.ui;

import atinka.dsa.Vec;
import atinka.model.Supplier;
import atinka.service.SupplierService;
import atinka.util.ConsoleIO;
import atinka.util.SimpleScreen;
import atinka.util.Tui;

public final class SupplierUI {
    private final SupplierService svc;
    private final SaveHooks saver;

    public interface SaveHooks { void saveSuppliers(); }

    public SupplierUI(SupplierService svc, SaveHooks saver){
        this.svc = svc; this.saver = saver;
    }

    public void show(){
        while (true){
            String[] algos = new String[]{ "Vec", "HashMapOpen", "MergeSort", "Linear scan" };
            String[] body = new String[]{
                    " 1) List all (by name)                 — MergeSort",
                    " 2) Add new                            — HashMapOpen uniqueness",
                    " 3) Update                             — HashMapOpen",
                    " 4) Remove                             — Linear scan + HashMapOpen",
                    " 5) Filter by location contains        — Linear scan",
                    " 6) Filter by turnaround days (<=)     — Linear scan",
                    "",
                    " 0) Back"
            };
            SimpleScreen.render("Suppliers — Manage", algos, body);

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
        SimpleScreen.render("Suppliers — All (by name)", new String[]{"MergeSort","Vec"}, new String[0]);
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
        SimpleScreen.render("Add Supplier", new String[]{"HashMapOpen index"}, new String[0]);
        String id = ConsoleIO.readLineOrCancel("ID"); if (id == null) return;
        if (svc.getById(id) != null){ Tui.toastWarn("ID exists."); pause(); return; }
        String name = ConsoleIO.readLineOrCancel("Name"); if (name == null) return;
        String contact = ConsoleIO.readLineOrCancel("Contact"); if (contact == null) return;
        String location = ConsoleIO.readLineOrCancel("Location"); if (location == null) return;
        int ta = ConsoleIO.readIntOrCancel("Turnaround days"); if (ta == Integer.MIN_VALUE) return;

        boolean ok = svc.add(new Supplier(id.trim(), name.trim(), contact.trim(), location.trim(), ta));
        if (ok){ saver.saveSuppliers(); Tui.toastSuccess("Added."); } else Tui.toastWarn("Failed.");
        pause();
    }

    private void update(){
        SimpleScreen.render("Update Supplier", new String[]{"HashMapOpen index"}, new String[0]);
        String id = ConsoleIO.readLineOrCancel("ID"); if (id == null) return;
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
        SimpleScreen.render("Remove Supplier", new String[]{"Linear scan","HashMapOpen index"}, new String[0]);
        String id = ConsoleIO.readLineOrCancel("ID"); if (id == null) return;
        boolean ok = svc.remove(id);
        if (ok){ saver.saveSuppliers(); Tui.toastSuccess("Removed."); } else Tui.toastWarn("Not found.");
        pause();
    }

    private void filterLocation(){
        SimpleScreen.render("Filter — Location contains", new String[]{"Linear scan"}, new String[0]);
        String term = ConsoleIO.readLineOrCancel("Term"); if (term == null) return;
        Vec<Supplier> v = svc.filterByLocationContains(term);
        if (v.size()==0){ Tui.toastInfo("No matches."); pause(); return; }
        for (int i=0;i<v.size();i++){
            Supplier s = v.get(i);
            ConsoleIO.println(s.getId() + "  " + s.getName() + "  " + s.getLocation());
        }
        pause();
    }

    private void filterTurnaround(){
        SimpleScreen.render("Filter — Turnaround <= days", new String[]{"Linear scan"}, new String[0]);
        int d = ConsoleIO.readIntOrCancel("Max days"); if (d == Integer.MIN_VALUE) return;
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
