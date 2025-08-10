package atinka.storage;

import atinka.model.PurchaseTxn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PurchaseLogCsv {
    private static final int COLS = 6; // id|drugCode|qty|timestamp|total|buyerId

    public void append(PurchaseTxn t) {
        Path p = PathsFS.purchaseLogPath();
        Path tmp = p.resolveSibling(p.getFileName().toString() + ".append.tmp");
        try (BufferedWriter w = Files.newBufferedWriter(tmp)) {
            String[] cols = new String[]{
                    t.getId(), t.getDrugCode(),
                    String.valueOf(t.getQty()),
                    t.getTimestamp().toString(),
                    String.valueOf(t.getTotal()),
                    t.getBuyerId()==null?"":t.getBuyerId()
            };
            w.write(CsvCodec.join(cols));
            w.newLine();
        } catch (Exception ex) {
            System.out.println("[WARN] Failed writing purchase tmp: " + ex.getMessage());
            return;
        }
        try {
            // append tmp -> main
            String line;
            try (BufferedReader r = Files.newBufferedReader(tmp);
                 BufferedWriter w = Files.newBufferedWriter(p, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
                while ((line = r.readLine()) != null) { w.write(line); w.newLine(); }
            }
            Files.deleteIfExists(tmp);
        } catch (Exception ex) {
            System.out.println("[WARN] Failed appending purchase log: " + ex.getMessage());
        }
    }
}
