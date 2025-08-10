package atinka.storage;

import atinka.dsa.HashSetOpen;
import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.util.DateUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public final class DrugCsvStore {
    private static final int COLS = 7; // code|name|price|stock|expiry|threshold|supplierIdsCSV

    public Vec<Drug> loadAll() {
        Vec<Drug> out = new Vec<>();
        Path p = PathsFS.drugsPath();
        int skipped = 0;
        try {
            if (!Files.exists(p)) return out;
            try (BufferedReader r = Files.newBufferedReader(p)) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.length() == 0) continue;
                    String[] c = CsvCodec.split(line, COLS);
                    String code = c[0], name = c[1];
                    if (code.length()==0 || name.length()==0) { skipped++; continue; }
                    double price = 0.0; try { price = Double.parseDouble(c[2]); } catch (Exception ignored) {}
                    int stock = 0;      try { stock = Integer.parseInt(c[3]); } catch (Exception ignored) {}
                    LocalDate expiry = DateUtil.parseDateOrNull(c[4]);
                    int thresh = 0;     try { thresh = Integer.parseInt(c[5]); } catch (Exception ignored) {}
                    Drug d = new Drug(code, name, price, stock, expiry, thresh);
                    // parse suppliers CSV (comma-separated)
                    if (c[6].length() > 0) {
                        String ids = c[6];
                        String cur = "";
                        for (int i = 0; i < ids.length(); i++) {
                            char ch = ids.charAt(i);
                            if (ch == ',') {
                                if (cur.length() > 0) { d.addSupplier(cur); cur = ""; }
                            } else { cur += ch; }
                        }
                        if (cur.length() > 0) d.addSupplier(cur);
                    }
                    out.add(d);
                }
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Failed reading drugs: " + ex.getMessage());
        }
        if (skipped > 0) System.out.println("[WARN] Skipped " + skipped + " malformed drug row(s).");
        return out;
    }

    public void saveAll(Vec<Drug> all) {
        Path p = PathsFS.drugsPath();
        Path tmp = p.resolveSibling(p.getFileName().toString() + ".tmp");
        Path bak = p.resolveSibling(p.getFileName().toString() + ".bak");
        try (BufferedWriter w = Files.newBufferedWriter(tmp)) {
            for (int i = 0; i < all.size(); i++) {
                Drug d = all.get(i);

                // build supplier CSV safely without java.util
                final StringBuilder sups = new StringBuilder();
                final boolean[] first = new boolean[]{true};
                HashSetOpen ids = d.getSupplierIds();
                if (ids != null) {
                    ids.forEach(s -> {
                        if (!first[0]) sups.append(',');
                        sups.append(s);
                        first[0] = false;
                    });
                }

                String[] cols = new String[]{
                        d.getCode(),
                        d.getName(),
                        String.valueOf(d.getPrice()),
                        String.valueOf(d.getStock()),
                        d.getExpiry()==null? "" : d.getExpiry().toString(),
                        String.valueOf(d.getReorderThreshold()),
                        sups.toString()
                };
                w.write(CsvCodec.join(cols));
                w.newLine();
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Failed writing tmp drugs: " + ex.getMessage());
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
