package atinka.storage;

import atinka.model.Customer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class CustomerCsvStore {
    private final Path file = PathsFS.CUSTOMERS;

    public List<Customer> loadAll() {
        List<Customer> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line; while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                var f = CsvCodec.split(line);
                list.add(new Customer(f.get(0), f.get(1), f.get(2)));
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return list;
    }

    public void saveAll(List<Customer> items) {
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Customer c : items) {
                String line = CsvCodec.join(c.getId(), c.getName(), c.getContact());
                bw.write(line); bw.newLine();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}