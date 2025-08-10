package atinka.storage;

import atinka.dsa.Vec;
import atinka.model.Customer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** CSV store for Customer using custom Vec. */
public class CustomerCsvStore {
    private final Path file = PathsFS.CUSTOMERS;

    public Vec<Customer> loadAll() {
        Vec<Customer> list = new Vec<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = CsvCodec.split(line);
                String id = f.length>0?f[0]:"";
                String name = f.length>1?f[1]:"";
                String contact = f.length>2?f[2]:"";
                list.add(new Customer(id, name, contact));
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return list;
    }

    public void saveAll(Vec<Customer> items) {
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < items.size(); i++) {
                Customer c = items.get(i);
                String line = CsvCodec.join(c.getId(), c.getName(), c.getContact());
                bw.write(line); bw.newLine();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}