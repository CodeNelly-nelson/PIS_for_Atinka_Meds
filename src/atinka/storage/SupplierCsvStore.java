package atinka.storage;

import atinka.dsa.Vec;
import atinka.model.Supplier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * suppliers.csv columns:
 * id,name,contact,location,turnaroundDays
 */
public final class SupplierCsvStore {

    public Vec<Supplier> load(){
        Vec<Supplier> out = new Vec<>();
        Path p = PathsFS.suppliersPath();
        if (!Files.exists(p)) return out;
        String[] lines = readAllLines(p);
        for (int i = 0; i < lines.length; i++){
            String ln = lines[i].trim();
            if (ln.length() == 0) continue;
            if (ln.startsWith("#")) continue;
            String[] cols = splitCsv(ln, 5);
            if (cols == null) continue;
            String id = cols[0];
            String name = cols[1];
            String contact = cols[2];
            String location = cols[3];
            int ta = parseInt(cols[4], 0);
            Supplier s = new Supplier(id, name, contact, location, ta);
            out.add(s);
        }
        return out;
    }

    public void saveAll(Vec<Supplier> src){
        Path p = PathsFS.suppliersPath();
        Path tmp = p.resolveSibling("suppliers.csv.tmp");
        StringBuilder sb = new StringBuilder();
        sb.append("# id,name,contact,location,turnaroundDays\n");
        for (int i = 0; i < src.size(); i++){
            Supplier s = src.get(i);
            sb.append(esc(s.getId())).append(',')
                    .append(esc(s.getName())).append(',')
                    .append(esc(s.getContact())).append(',')
                    .append(esc(s.getLocation())).append(',')
                    .append(s.getTurnaroundDays()).append('\n');
        }
        byte[] bytes;
        try { bytes = sb.toString().getBytes("UTF-8"); } catch(Exception e){ bytes = new byte[0]; }
        try {
            Files.write(tmp, bytes);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e){
            try { Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING); } catch(Exception ignored){}
        }
    }

    // ---------- helpers ----------

    private String[] readAllLines(Path p){
        try {
            byte[] b = Files.readAllBytes(p);
            String s = new String(b, "UTF-8");
            s = s.replace("\r\n", "\n").replace('\r', '\n');
            return splitLines(s);
        } catch (Exception e){
            return new String[0];
        }
    }

    private String[] splitLines(String s){
        int count = 1;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '\n') count++;
        String[] out = new String[count];
        int idx = 0, start = 0;
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) == '\n'){
                out[idx++] = s.substring(start, i);
                start = i + 1;
            }
        }
        out[idx] = s.substring(start);
        return out;
    }

    private String[] splitCsv(String line, int n){
        String[] out = new String[n];
        int idx = 0, start = 0;
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

    private int parseInt(String s, int def){
        try { return Integer.parseInt(s); } catch(Exception e){ return def; }
    }

    private String esc(String s){
        if (s == null) return "";
        StringBuilder b = new StringBuilder(s.length());
        for (int i=0;i<s.length();i++){
            char c=s.charAt(i);
            if (c==',' || c=='\n' || c=='\r') continue;
            b.append(c);
        }
        return b.toString().trim();
    }
}
