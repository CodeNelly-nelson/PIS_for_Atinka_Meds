package atinka.storage;

import atinka.dsa.Vec;
import atinka.model.Customer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CustomerCsvStore {
    private static final int COLS = 3; // id|name|contact

    public Vec<Customer> loadAll() {
        Vec<Customer> out = new Vec<>();
        Path p = PathsFS.customersPath();
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
                    String contact=c[2];
                    Customer cu = new Customer(id,name,contact);
                    out.add(cu);
                }
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Failed reading customers: " + ex.getMessage());
        }
        if (skipped > 0) System.out.println("[WARN] Skipped " + skipped + " malformed customer row(s).");
        return out;
    }

    public void saveAll(Vec<Customer> all) {
        Path p = PathsFS.customersPath();
        Path tmp = p.resolveSibling(p.getFileName().toString() + ".tmp");
        Path bak = p.resolveSibling(p.getFileName().toString() + ".bak");
        try (BufferedWriter w = Files.newBufferedWriter(tmp)) {
            for (int i = 0; i < all.size(); i++) {
                Customer c = all.get(i);
                String[] cols = new String[]{ c.getId(), c.getName(), c.getContact() };
                w.write(CsvCodec.join(cols));
                w.newLine();
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Failed writing tmp customers: " + ex.getMessage());
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
