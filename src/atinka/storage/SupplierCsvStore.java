package atinka.storage;

import atinka.dsa.Vec;
import atinka.model.Supplier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SupplierCsvStore {
    private static final int COLS = 5; // id|name|location|turnaround|contact

    public Vec<Supplier> loadAll() {
        Vec<Supplier> out = new Vec<>();
        Path p = PathsFS.suppliersPath();
        int skipped = 0;
        try {
            if (!Files.exists(p)) return out;
            try (BufferedReader r = Files.newBufferedReader(p)) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.length() == 0) continue;
                    String[] c = CsvCodec.split(line, COLS);
                    String id=c[0], name=c[1];
                    if (id.length()==0 || name.length()==0) { skipped++; continue; }
                    String loc=c[2], contact=c[4];
                    int ta=0; try{ ta=Integer.parseInt(c[3]); }catch(Exception ignored){}
                    Supplier s = new Supplier(id,name,loc,ta,contact);
                    out.add(s);
                }
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Failed reading suppliers: " + ex.getMessage());
        }
        if (skipped > 0) System.out.println("[WARN] Skipped " + skipped + " malformed supplier row(s).");
        return out;
    }

    public void saveAll(Vec<Supplier> all) {
        Path p = PathsFS.suppliersPath();
        Path tmp = p.resolveSibling(p.getFileName().toString() + ".tmp");
        Path bak = p.resolveSibling(p.getFileName().toString() + ".bak");
        try (BufferedWriter w = Files.newBufferedWriter(tmp)) {
            for (int i = 0; i < all.size(); i++) {
                Supplier s = all.get(i);
                String[] cols = new String[]{
                        s.getId(), s.getName(), s.getLocation(),
                        String.valueOf(s.getTurnaroundDays()), s.getContact()
                };
                w.write(CsvCodec.join(cols));
                w.newLine();
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Failed writing tmp suppliers: " + ex.getMessage());
            return;
        }
        try {
            if (Files.exists(p)) Files.copy(p, bak, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.move(tmp, p, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception moveEx) {
            try { Files.move(tmp, p, java.nio.file.StandardCopyOption.REPLACE_EXISTING); } catch (Exception ignore) {}
        }
    }
}
