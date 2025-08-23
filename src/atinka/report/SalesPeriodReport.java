package atinka.report;

import atinka.dsa.Comparator;
import atinka.dsa.HashMapOpen;
import atinka.dsa.MergeSort;
import atinka.dsa.Vec;
import atinka.model.Drug;
import atinka.model.SaleTxn;
import atinka.service.DrugService;
import atinka.storage.ReportsFS;
import atinka.storage.SaleLogCsv;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Accounting / sales report for a specified period [from, to].
 * Output goes to data/reports/sales_period.txt (single file, overwritten).
 */
public final class SalesPeriodReport {
    private SalesPeriodReport(){}

    public static java.nio.file.Path generate(LocalDateTime from, LocalDateTime to,
                                              SaleLogCsv sales, DrugService drugs){
        if (from == null) from = LocalDateTime.MIN.plusYears(1); // avoid MIN overflow printing
        if (to == null)   to   = LocalDateTime.MAX.minusYears(1);

        // 1) Read + filter
        Vec<SaleTxn> all = sales.readAll();
        Vec<SaleTxn> filt = new Vec<>();
        for (int i=0;i<all.size();i++){
            SaleTxn s = all.get(i);
            LocalDateTime ts = s.getTimestamp();
            if (ts == null) continue;
            if ((ts.isAfter(from) || ts.isEqual(from)) && (ts.isBefore(to) || ts.isEqual(to))){
                filt.add(s);
            }
        }

        // 2) Accumulate totals, per-drug, per-day
        Totals grand = new Totals();
        HashMapOpen<Acc> perDrug = new HashMapOpen<>(64);
        HashMapOpen<Day> perDay = new HashMapOpen<>(64);

        for (int i=0;i<filt.size();i++){
            SaleTxn s = filt.get(i);
            grand.count++;
            grand.units += s.getQty();
            grand.revenue += s.getTotal();

            // per-drug
            String code = s.getCode()==null? "": s.getCode();
            Acc a = perDrug.get(code);
            if (a == null){ a = new Acc(); a.code = code; a.qty = 0; a.revenue = 0; perDrug.put(code, a); }
            a.qty += s.getQty();
            a.revenue += s.getTotal();

            // per-day
            String day = toDayKey(s.getTimestamp());
            Day d = perDay.get(day);
            if (d == null){ d = new Day(); d.day = day; d.revenue = 0; perDay.put(day, d); }
            d.revenue += s.getTotal();
        }

        // 3) Convert perDrug and perDay to Vecs
        Vec<Acc> rows = new Vec<>();
        perDrug.forEach((k,v) -> rows.add(v));
        // enrich with drug name if available
        for (int i=0;i<rows.size();i++){
            Acc a = rows.get(i);
            Drug d = drugs.getByCode(a.code);
            a.name = (d==null? "" : d.getName());
        }

        Vec<Day> days = new Vec<>();
        perDay.forEach((k,v) -> days.add(v));

        // 4) Sort per-drug by revenue desc; per-day by ascending date (lex OK)
        Comparator<Acc> byRevDesc = (x,y) -> {
            double diff = x.revenue - y.revenue;
            if (diff < 0) return 1;
            if (diff > 0) return -1;
            // tie-break by name then code
            int c = compareIgnoreCase(x.name, y.name);
            if (c != 0) return c;
            return compareIgnoreCase(x.code, y.code);
        };
        MergeSort.sort(rows, byRevDesc);

        Comparator<Day> byDayAsc = (x,y) -> compareIgnoreCase(x.day, y.day);
        MergeSort.sort(days, byDayAsc);

        // 5) Emit
        StringBuilder out = new StringBuilder();
        out.append("Atinka Meds — Sales & Accounting Report\n");
        out.append("Period: ").append(from).append("  to  ").append(to).append("\n");
        out.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

        out.append("Summary\n");
        out.append("- Transactions: ").append(grand.count).append("\n");
        out.append("- Units sold:   ").append(grand.units).append("\n");
        out.append("- Revenue:      ").append(toFixed2(grand.revenue)).append("\n\n");

        out.append("Per-drug breakdown (sorted by revenue desc)\n");
        out.append(StringPad.padRight("CODE", 10)).append("  ")
                .append(StringPad.padRight("NAME", 28)).append("  ")
                .append(StringPad.padLeft("QTY", 6)).append("  ")
                .append(StringPad.padLeft("REVENUE", 10)).append("\n");
        for (int i=0;i<rows.size();i++){
            Acc a = rows.get(i);
            out.append(StringPad.padRight(a.code, 10)).append("  ")
                    .append(StringPad.padRight(limit(a.name,28), 28)).append("  ")
                    .append(StringPad.padLeft(String.valueOf(a.qty), 6)).append("  ")
                    .append(StringPad.padLeft(toFixed2(a.revenue), 10)).append("\n");
        }
        out.append("\n");

        out.append("Per-day revenue\n");
        out.append(StringPad.padRight("DAY", 12)).append("  ")
                .append(StringPad.padLeft("REVENUE", 10)).append("\n");
        for (int i=0;i<days.size();i++){
            Day d = days.get(i);
            out.append(StringPad.padRight(d.day, 12)).append("  ")
                    .append(StringPad.padLeft(toFixed2(d.revenue), 10)).append("\n");
        }

        return ReportsFS.writeReport("sales_period.txt", out.toString());
    }

    // -------- models for rows --------
    private static final class Totals { int count; int units; double revenue; }
    private static final class Acc { String code; String name; int qty; double revenue; }
    private static final class Day { String day; double revenue; }

    // -------- helpers (no java.util) --------
    private static String toDayKey(LocalDateTime t){
        LocalDate d = (t==null) ? null : t.toLocalDate();
        return d==null ? "" : d.toString(); // yyyy-mm-dd (lexical = chronological)
    }

    private static int compareIgnoreCase(String a, String b){
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        int na=a.length(), nb=b.length();
        int n = na < nb ? na : nb;
        for (int i=0;i<n;i++){
            char ca = toLower(a.charAt(i));
            char cb = toLower(b.charAt(i));
            if (ca != cb) return (ca < cb) ? -1 : 1;
        }
        if (na == nb) return 0;
        return (na < nb) ? -1 : 1;
    }
    private static char toLower(char c){ return (c>='A'&&c<='Z')?(char)(c+32):c; }

    private static String toFixed2(double x){
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

    private static String limit(String s, int max){
        if (s == null) return "";
        if (s.length() <= max) return s;
        if (max <= 1) return s.substring(0, max);
        return s.substring(0, max-1) + "…";
    }

    /** Tiny left/right padding helper (no java.util). */
    private static final class StringPad {
        static String padRight(String s, int w){
            if (s == null) s = "";
            int n = w - s.length();
            if (n <= 0) return s;
            StringBuilder b = new StringBuilder(w);
            b.append(s);
            for (int i=0;i<n;i++) b.append(' ');
            return b.toString();
        }
        static String padLeft(String s, int w){
            if (s == null) s = "";
            int n = w - s.length();
            if (n <= 0) return s;
            StringBuilder b = new StringBuilder(w);
            for (int i=0;i<n;i++) b.append(' ');
            b.append(s);
            return b.toString();
        }
    }
}
