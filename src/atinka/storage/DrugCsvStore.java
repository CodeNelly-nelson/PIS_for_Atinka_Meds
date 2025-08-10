package atinka.storage;

import atinka.model.Drug;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class DrugCsvStore {
    private final Path file = PathsFS.DRUGS;

    public List<Drug> loadAll() {
        List<Drug> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> f = CsvCodec.split(line);
                // code|name|price|stock|expiry|threshold|supplierIds(;)
                String code = f.get(0);
                String name = f.get(1);
                double price = Double.parseDouble(f.get(2));
                int stock = Integer.parseInt(f.get(3));
                LocalDate expiry = LocalDate.parse(f.get(4));
                int threshold = Integer.parseInt(f.get(5));
                Set<String> supplierIds = new HashSet<>();
                if (f.size() > 6 && !f.get(6).isEmpty()) supplierIds.addAll(Arrays.asList(f.get(6).split(";")));
                Drug d = new Drug(code, name, price, stock, expiry, threshold);
                for (String s : supplierIds) d.addSupplier(s);
                list.add(d);
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return list;
    }

    public void saveAll(List<Drug> drugs) {
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Drug d : drugs) {
                String suppliers = String.join(";", d.getSupplierIds());
                String line = CsvCodec.join(
                        d.getCode(), d.getName(), String.valueOf(d.getPrice()), String.valueOf(d.getStock()),
                        d.getExpiry().toString(), String.valueOf(d.getReorderThreshold()), suppliers
                );
                bw.write(line); bw.newLine();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}