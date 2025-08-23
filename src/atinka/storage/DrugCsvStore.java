package atinka.storage;

import atinka.dsa.HashSetOpen;
import atinka.dsa.Vec;
import atinka.model.Drug;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

/**
 * drugs.csv columns:
 * code,name,price,stock,expiry,threshold,suppliers
 * suppliers is pipe-separated supplier IDs, e.g.: S0001|S0003
 */
public final class DrugCsvStore {
    public DrugCsvStore(){}

    public Vec<Drug> load(){
        Vec<Drug> out = new Vec<>();
        Path p = PathsFS.drugsPath();
        if (!Files.exists(p)) return out;
        String[] lines = readAllLines(p);
        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i].trim();
            if (ln.length() == 0) continue;
            if (ln.startsWith("#")) continue; // allow comments
            String[] cols = splitCsv(ln, 7);
            if (cols == null) continue;
            String code = cols[0];
            String name = cols[1];
            double price = parseDouble(cols[2], 0.0);
            int stock = parseInt(cols[3], 0);
            LocalDate expiry = parseDate(cols[4]);
            int threshold = parseInt(cols[5], 0);
            Drug d = new Drug(code, name, price, stock, expiry, threshold);

            // suppliers
            String supField = cols[6];
            if (supField != null && supField.length() > 0) {
                String[] ids = splitPipe(supField);
                for (int s = 0; s < ids.length; s++) {
                    String id = ids[s].trim();
                    if (id.length() > 0) d.addSupplier(id);
                }
            }
            out.add(d);
        }
        return out;
    }

    public void saveAll(Vec<Drug> drugs){
        Path p = PathsFS.drugsPath();
        Path tmp = p.resolveSibling("drugs.csv.tmp");
        StringBuilder sb = new StringBuilder();
        sb.append("# code,name,price,stock,expiry,threshold,suppliers\n");
        for (int i = 0; i < drugs.size(); i++) {
            Drug d = drugs.get(i);
            sb.append(escape(d.getCode())).append(',')
                    .append(escape(d.getName())).append(',')
                    .append(toFixed2(d.getPrice())).append(',')
                    .append(d.getStock()).append(',')
                    .append(formatDate(d.getExpiry())).append(',')
                    .append(d.getThreshold()).append(',')
                    .append(joinSuppliers(d.suppliers()))
                    .append('\n');
        }
        byte[] bytes;
        try { bytes = sb.toString().getBytes("UTF-8"); } catch(Exception e){ bytes = new byte[0]; }
        try {
            Files.write(tmp, bytes);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            try { Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING); } catch(Exception ignored){}
        }
    }

    // ---------- helpers (no java.util) ----------

    private String[] readAllLines(Path p){
        try {
            byte[] b = Files.readAllBytes(p);
            String s = new String(b, "UTF-8");
            // Normalize line endings
            s = s.replace("\r\n", "\n").replace('\r', '\n');
            return splitLines(s);
        } catch (Exception e){
            return new String[0];
        }
    }

    private String[] splitLines(String s){
        // Count lines
        int count = 1;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '\n') count++;
        String[] out = new String[count];
        int idx = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) == '\n'){
                out[idx++] = s.substring(start, i);
                start = i + 1;
            }
        }
        out[idx] = s.substring(start);
        return out;
    }

    /** Trivial CSV split (no quotes). Expected columns = n. */
    private String[] splitCsv(String line, int n){
        String[] out = new String[n];
        int idx = 0;
        int start = 0;
        for (int i = 0; i < line.length(); i++){
            if (line.charAt(i) == ','){
                out[idx++] = safeSub(line, start, i);
                start = i + 1;
                if (idx == n - 1) break;
            }
        }
        out[idx++] = safeSub(line, start, line.length());
        if (idx != n) return null;
        return out;
    }

    private String safeSub(String s, int a, int b){
        if (a < 0) a = 0; if (b < a) b = a; if (b > s.length()) b = s.length();
        return s.substring(a, b);
    }

    private String[] splitPipe(String s){
        // count
        int count = 1;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '|') count++;
        String[] out = new String[count];
        int idx = 0, start = 0;
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) == '|'){
                out[idx++] = safeSub(s, start, i);
                start = i + 1;
            }
        }
        out[idx] = safeSub(s, start, s.length());
        return out;
    }

    private String joinSuppliers(HashSetOpen set){
        final StringBuilder sb = new StringBuilder();
        set.forEach(id -> {
            if (sb.length() > 0) sb.append('|');
            sb.append(escape(id));
        });
        return sb.toString();
    }

    private LocalDate parseDate(String s){
        try { return (s == null || s.length() == 0) ? null : LocalDate.parse(s); }
        catch (Exception e){ return null; }
    }

    private String formatDate(LocalDate d){
        return d == null ? "" : d.toString();
    }

    private int parseInt(String s, int def){
        try { return Integer.parseInt(s); } catch(Exception e){ return def; }
    }

    private double parseDouble(String s, double def){
        try { return Double.parseDouble(s); } catch(Exception e){ return def; }
    }

    private String escape(String s){
        if (s == null) return "";
        // very light sanitize for CSV: strip commas/newlines
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if (c == ',' || c == '\n' || c == '\r') continue;
            b.append(c);
        }
        return b.toString().trim();
    }

    private String toFixed2(double x){
        long m = Math.round(x * 100.0);
        String sign = m < 0 ? "-" : "";
        if (m < 0) m = -m;
        long i = m / 100;
        long f = m % 100;
        StringBuilder sb = new StringBuilder();
        sb.append(sign).append(i).append('.');
        if (f < 10) sb.append('0');
        sb.append(f);
        return sb.toString();
    }
}
