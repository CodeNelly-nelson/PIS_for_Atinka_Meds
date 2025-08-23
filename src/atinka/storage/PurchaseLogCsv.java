package atinka.storage;

import atinka.dsa.Vec;
import atinka.model.PurchaseTxn;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

/**
 * purchases.csv columns:
 * timestamp,code,qty,buyerId,unitCost,total
 * Example: 2025-07-01T10:20:30,AMOX500,50,STAFF01,2.20,110.00
 */
public final class PurchaseLogCsv {

    public void append(PurchaseTxn t){
        Path p = PathsFS.purchaseLogPath();
        StringBuilder sb = new StringBuilder();
        sb.append(formatDateTime(t.getTimestamp())).append(',')
                .append(esc(t.getCode())).append(',')
                .append(t.getQty()).append(',')
                .append(esc(t.getBuyerId())).append(',')
                .append(toFixed2(t.getUnitCost())).append(',')
                .append(toFixed2(t.getTotal())).append('\n');
        try {
            byte[] bytes = sb.toString().getBytes("UTF-8");
            if (!Files.exists(p)) {
                // create with header
                String header = "# timestamp,code,qty,buyerId,unitCost,total\n";
                Files.write(p, header.getBytes("UTF-8"));
            }
            Files.write(p, bytes, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch(Exception ignored){}
    }

    public Vec<PurchaseTxn> readAll(){
        Vec<PurchaseTxn> out = new Vec<>();
        Path p = PathsFS.purchaseLogPath();
        if (!Files.exists(p)) return out;
        String[] lines = readAllLines(p);
        for (int i=0;i<lines.length;i++){
            String ln = lines[i].trim();
            if (ln.length()==0 || ln.startsWith("#")) continue;
            String[] c = splitCsv(ln, 6);
            if (c == null) continue;
            LocalDateTime ts = parseDateTime(c[0]);
            String code = c[1];
            int qty = parseInt(c[2], 0);
            String buyer = c[3];
            double unit = parseDouble(c[4], 0.0);
            double total = parseDouble(c[5], 0.0);
            out.add(new PurchaseTxn(ts, code, qty, buyer, unit, total));
        }
        return out;
    }

    // ---------- helpers ----------

    private String[] readAllLines(Path p){
        try {
            byte[] b = Files.readAllBytes(p);
            String s = new String(b, "UTF-8");
            s = s.replace("\r\n","\n").replace('\r','\n');
            return splitLines(s);
        } catch (Exception e){ return new String[0]; }
    }

    private String[] splitLines(String s){
        int count=1;
        for (int i=0;i<s.length();i++) if (s.charAt(i)=='\n') count++;
        String[] out=new String[count];
        int idx=0,start=0;
        for (int i=0;i<s.length();i++){
            if (s.charAt(i)=='\n'){ out[idx++]=s.substring(start,i); start=i+1; }
        }
        out[idx]=s.substring(start);
        return out;
    }

    private String[] splitCsv(String line, int n){
        String[] out=new String[n];
        int idx=0,start=0;
        for (int i=0;i<line.length();i++){
            if (line.charAt(i)==','){
                out[idx++]=safeSub(line,start,i);
                start=i+1;
                if (idx==n-1) break;
            }
        }
        out[idx++]=safeSub(line,start,line.length());
        if (idx!=n) return null;
        return out;
    }

    private String safeSub(String s,int a,int b){
        if (a<0) a=0; if (b<a) b=a; if (b>s.length()) b=s.length();
        return s.substring(a,b);
    }

    private String esc(String s){
        if (s==null) return "";
        StringBuilder b=new StringBuilder(s.length());
        for (int i=0;i<s.length();i++){
            char c=s.charAt(i);
            if (c==','||c=='\n'||c=='\r') continue;
            b.append(c);
        }
        return b.toString().trim();
    }

    private int parseInt(String s,int def){ try { return Integer.parseInt(s);} catch(Exception e){ return def; } }
    private double parseDouble(String s,double def){ try { return Double.parseDouble(s);} catch(Exception e){ return def; } }

    private LocalDateTime parseDateTime(String s){ try { return LocalDateTime.parse(s);} catch(Exception e){ return LocalDateTime.now(); } }
    private String formatDateTime(LocalDateTime t){ return t==null? "": t.toString(); }

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
