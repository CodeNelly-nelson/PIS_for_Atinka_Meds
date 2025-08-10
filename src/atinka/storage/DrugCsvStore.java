package atinka.storage;

import atinka.dsa.Vec;
import atinka.model.Drug;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

/**
 * CSV persistence for Drug using only custom core collections.
 * Format per line: code|name|price|stock|expiry|threshold|supplierIds(;)
 */
public class DrugCsvStore {
    private final Path file = PathsFS.DRUGS;

    public Vec<Drug> loadAll() {
        Vec<Drug> list = new Vec<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = CsvCodec.split(line);
                String code = f.length > 0 ? f[0] : "";
                String name = f.length > 1 ? f[1] : "";
                double price = f.length > 2 ? parseDoubleSafe(f[2]) : 0.0;
                int stock = f.length > 3 ? parseIntSafe(f[3]) : 0;
                LocalDate expiry = f.length > 4 ? LocalDate.parse(f[4]) : LocalDate.now();
                int threshold = f.length > 5 ? parseIntSafe(f[5]) : 0;
                Drug d = new Drug(code, name, price, stock, expiry, threshold);
                if (f.length > 6 && f[6] != null && !f[6].isEmpty()) {
                    String s = f[6]; int start = 0; int i;
                    while (true) {
                        i = s.indexOf(';', start);
                        String tok = (i == -1) ? s.substring(start) : s.substring(start, i);
                        if (!tok.isEmpty()) d.addSupplier(tok);
                        if (i == -1) break; else start = i + 1;
                    }
                }
                list.add(d);
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return list;
    }

    public void saveAll(Vec<Drug> drugs) {
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int idx = 0; idx < drugs.size(); idx++) {
                Drug d = drugs.get(idx);
                String suppliers = joinSuppliers(d);
                String line = CsvCodec.join(
                        d.getCode(), d.getName(), String.valueOf(d.getPrice()), String.valueOf(d.getStock()),
                        d.getExpiry().toString(), String.valueOf(d.getReorderThreshold()), suppliers
                );
                bw.write(line); bw.newLine();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private static int parseIntSafe(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
    private static double parseDoubleSafe(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; } }

    private static String joinSuppliers(Drug d) {
        final StringBuilder sb = new StringBuilder();
        d.getSupplierIds().forEach(s -> {
            if (s != null && !s.isEmpty()) {
                if (sb.length() > 0) sb.append(';');
                sb.append(s);
            }
        });
        return sb.toString();
    }
}