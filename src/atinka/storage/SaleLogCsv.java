package atinka.storage;

import atinka.model.SaleTxn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

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

    /** Reads entire log as a freshly allocated array (no java.util). */
    public SaleTxn[] readAll() {
        int count = 0;
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            while (true) { String line = br.readLine(); if (line == null) break; if (!line.isBlank()) count++; }
        } catch (IOException e) { throw new RuntimeException(e); }
        SaleTxn[] arr = new SaleTxn[count];
        int i = 0;
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = CsvCodec.split(line);
                arr[i++] = new SaleTxn(
                        f.length>0?f[0]:"",
                        f.length>1?f[1]:"",
                        f.length>2?parseIntSafe(f[2]):0,
                        f.length>3?f[3]:"",
                        f.length>4?LocalDateTime.parse(f[4]):LocalDateTime.now(),
                        f.length>5?parseDoubleSafe(f[5]):0.0
                );
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return arr;
    }

    private static int parseIntSafe(String s){ try{ return Integer.parseInt(s);}catch(Exception e){return 0;} }
    private static double parseDoubleSafe(String s){ try{ return Double.parseDouble(s);}catch(Exception e){return 0.0;} }
}