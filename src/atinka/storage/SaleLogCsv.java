package atinka.storage;

import atinka.model.SaleTxn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public final class SaleLogCsv {
    private static final int COLS = 6; // id|drugCode|qty|timestamp|total|customerId

    public void append(SaleTxn t) {
        Path p = PathsFS.salesLogPath();
        Path tmp = p.resolveSibling(p.getFileName().toString() + ".append.tmp");
        try (BufferedWriter w = Files.newBufferedWriter(tmp)) {
            String[] cols = new String[]{
                    t.getId(), t.getDrugCode(),
                    String.valueOf(t.getQty()),
                    t.getTimestamp().toString(),
                    String.valueOf(t.getTotal()),
                    t.getCustomerId()==null?"":t.getCustomerId()
            };
            w.write(CsvCodec.join(cols));
            w.newLine();
        } catch (Exception ex) {
            System.out.println("[WARN] Failed writing sales tmp: " + ex.getMessage());
            return;
        }
        try {
            String line;
            try (BufferedReader r = Files.newBufferedReader(tmp);
                 BufferedWriter w = Files.newBufferedWriter(p, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
                while ((line = r.readLine()) != null) { w.write(line); w.newLine(); }
            }
            Files.deleteIfExists(tmp);
        } catch (Exception ex) {
            System.out.println("[WARN] Failed appending sales log: " + ex.getMessage());
        }
    }

    public SaleTxn[] readAll() {
        Path p = PathsFS.salesLogPath();
        if (!Files.exists(p)) return new SaleTxn[0];
        // first pass: count
        int count = 0;
        try (BufferedReader r = Files.newBufferedReader(p)) {
            while (r.readLine() != null) count++;
        } catch (Exception ignored) {}
        SaleTxn[] out = new SaleTxn[count];
        int idx = 0;
        try (BufferedReader r = Files.newBufferedReader(p)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] c = CsvCodec.split(line, COLS);
                try {
                    String id = c[0], code = c[1];
                    int qty = Integer.parseInt(c[2]);
                    LocalDateTime ts = LocalDateTime.parse(c[3]);
                    double total = Double.parseDouble(c[4]);
                    String cust = c[5];
                    out[idx++] = new SaleTxn(id, code, qty, cust, ts, total);
                } catch (Exception ignoreBadRow) { /* skip malformed */ }
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Failed reading sales log: " + ex.getMessage());
        }
        if (idx < out.length) {
            SaleTxn[] shrunk = new SaleTxn[idx];
            for (int i=0;i<idx;i++) shrunk[i] = out[i];
            return shrunk;
        }
        return out;
    }
}
