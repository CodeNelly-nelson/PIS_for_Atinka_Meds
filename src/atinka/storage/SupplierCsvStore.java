package atinka.storage;

import atinka.dsa.Vec;
import atinka.model.Supplier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** CSV store for Supplier using custom Vec. */
public class SupplierCsvStore {
    private final Path file = PathsFS.SUPPLIERS;

    public Vec<Supplier> loadAll() {
        Vec<Supplier> list = new Vec<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = CsvCodec.split(line);
                String id = f.length>0?f[0]:"";
                String name = f.length>1?f[1]:"";
                String loc = f.length>2?f[2]:"";
                int ta = f.length>3?parseIntSafe(f[3]):0;
                String contact = f.length>4?f[4]:"";
                list.add(new Supplier(id, name, loc, ta, contact));
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return list;
    }

    public void saveAll(Vec<Supplier> items) {
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < items.size(); i++) {
                Supplier s = items.get(i);
                String line = CsvCodec.join(s.getId(), s.getName(), s.getLocation(), String.valueOf(s.getTurnaroundDays()), s.getContact());
                bw.write(line); bw.newLine();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private static int parseIntSafe(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
}