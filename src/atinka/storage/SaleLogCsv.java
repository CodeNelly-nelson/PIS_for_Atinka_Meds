package atinka.storage;

import atinka.model.SaleTxn;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/** Append-only log for sales. */
public class SaleLogCsv {
    private final Path file = PathsFS.SALES_LOG;

    public void append(SaleTxn t) {
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            String line = CsvCodec.join(t.getId(), t.getDrugCode(), String.valueOf(t.getQty()), t.getCustomerId(),
                    t.getTimestamp().toString(), String.valueOf(t.getTotal()));
            bw.write(line); bw.newLine();
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public List<SaleTxn> readAll() {
        List<SaleTxn> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line; while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                var f = CsvCodec.split(line);
                list.add(new SaleTxn(
                        f.get(0), f.get(1), Integer.parseInt(f.get(2)), f.get(3),
                        LocalDateTime.parse(f.get(4)), Double.parseDouble(f.get(5))
                ));
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return list;
    }
}