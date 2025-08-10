package atinka.storage;

import atinka.model.Supplier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class SupplierCsvStore {
    private final Path file = PathsFS.SUPPLIERS;

    public List<Supplier> loadAll() {
        List<Supplier> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line; while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                var f = CsvCodec.split(line);
                Supplier s = new Supplier(f.get(0), f.get(1), f.get(2), Integer.parseInt(f.get(3)), f.get(4));
                list.add(s);
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return list;
    }

    public void saveAll(List<Supplier> items) {
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Supplier s : items) {
                String line = CsvCodec.join(s.getId(), s.getName(), s.getLocation(), String.valueOf(s.getTurnaroundDays()), s.getContact());
                bw.write(line); bw.newLine();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}