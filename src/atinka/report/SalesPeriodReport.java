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

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sales + Accounting report for a specified period (inclusive).
 * Source: SaleLogCsv.readAll() -> SaleTxn[]
 * Writes a single file: data/reports/sales.txt
 */
public final class SalesPeriodReport {
    private SalesPeriodReport(){}

    public static Path generate(DrugService drugs, SaleLogCsv sales, LocalDateTime from, LocalDateTime to) {
        SaleTxn[] all = sales.readAll();

        // ---------- accumulate ----------
        double totalRevenue = 0.0;
        int totalUnits = 0;
        int transactions = 0;

        // per-drug accumulation: code -> Tot
        HashMapOpen<Tot> perDrug = new HashMapOpen<>();
        // per-day accumulation: yyyy-MM-dd -> revenue
        HashMapOpen<DoubleBox> perDay = new HashMapOpen<>();

        for (int i=0;i<all.length;i++){
            SaleTxn s = all[i];
            if (s == null) continue;
            LocalDateTime ts = s.getTimestamp();
            if (ts.isBefore(from) || ts.isAfter(to)) continue;

            transactions++;
            totalRevenue += s.getTotal();
            totalUnits += s.getQty();

            // per drug
            Tot t = perDrug.get(s.getDrugCode());
            if (t == null) {
                Drug d = drugs.getByCode(s.getDrugCode());
                String name = (d == null) ? "" : d.getName();
                t = new Tot(s.getDrugCode(), name);
                perDrug.put(s.getDrugCode(), t);
            }
            t.qty += s.getQty();
            t.revenue += s.getTotal();

            // per day
            String dayKey = ts.toLocalDate().toString();
            DoubleBox db = perDay.get(dayKey);
            if (db == null) { db = new DoubleBox(); perDay.put(dayKey, db); }
            db.value += s.getTotal();
        }

        // ---------- per-drug vec + sort by revenue desc ----------
        Vec<Tot> rows = new Vec<>();
        perDrug.forEach((code, t) -> { if (t != null) rows.add(t); });
        MergeSort.sort(rows, new Comparator<Tot>() {
            @Override public int compare(Tot a, Tot b) {
                int c = Double.compare(b.revenue, a.revenue); // desc
                if (c != 0) return c;
                return a.code.compareToIgnoreCase(b.code);
            }
        });

        // ---------- per-day vec in chronological order ----------
        Vec<DayLine> days = new Vec<>();
        LocalDate d = from.toLocalDate();
        LocalDate end = to.toLocalDate();
        while (!d.isAfter(end)) {
            String key = d.toString();
            DoubleBox box = perDay.get(key);
            double v = (box == null) ? 0.0 : box.value;
            days.add(new DayLine(key, v));
            d = d.plusDays(1);
        }

        // ---------- render ----------
        StringBuilder out = new StringBuilder();
        out.append("Atinka Meds — Sales & Accounting Report\n");
        out.append("Generated: ").append(LocalDateTime.now()).append("\n");
        out.append("Period: ").append(from).append("  to  ").append(to).append("\n\n");

        out.append("Summary\n");
        out.append("- Transactions: ").append(transactions).append("\n");
        out.append("- Units sold:   ").append(totalUnits).append("\n");
        out.append("- Revenue:      ").append(toFixed2(totalRevenue)).append("\n");
        out.append("- Avg ticket:   ").append(transactions==0 ? "0.00" : toFixed2(totalRevenue / transactions)).append("\n\n");

        out.append("Per-Drug Breakdown (sorted by revenue desc)\n");
        if (rows.size() == 0) {
            out.append("  No sales in this period.\n\n");
        } else {
            out.append(pad("CODE", 10)).append(pad("NAME", 28))
                    .append(padRight("QTY", 8)).append(padRight("REVENUE", 10)).append("\n");
            for (int i=0;i<rows.size();i++){
                Tot t2 = rows.get(i);
                out.append(pad(t2.code,10))
                        .append(pad(trunc(t2.name,28),28))
                        .append(padRight(String.valueOf(t2.qty),8))
                        .append(padRight(toFixed2(t2.revenue),10))
                        .append("\n");
            }
            out.append("\n");
        }

        out.append("Per-Day Revenue\n");
        if (days.size() == 0) {
            out.append("  (none)\n");
        } else {
            out.append(pad("DATE", 12)).append(padRight("REVENUE", 10)).append("\n");
            for (int i=0;i<days.size();i++){
                DayLine dl = days.get(i);
                out.append(pad(dl.date,12)).append(padRight(toFixed2(dl.revenue),10)).append("\n");
            }
        }

        return ReportsFS.writeSales(out.toString());
    }

    // -------- types --------
    private static final class Tot {
        final String code; String name; int qty; double revenue;
        Tot(String code, String name){ this.code=code; this.name=name==null?"":name; }
    }
    private static final class DayLine {
        final String date; final double revenue;
        DayLine(String date, double revenue){ this.date=date; this.revenue=revenue; }
    }
    private static final class DoubleBox { double value; }

    // -------- tiny text helpers --------
    private static String toFixed2(double x) {
        long m = Math.round(x * 100.0);
        String sign = m < 0 ? "-" : "";
        if (m < 0) m = -m;
        long i = m/100;
        long f = m%100;
        StringBuilder sb = new StringBuilder();
        sb.append(sign).append(i).append('.');
        if (f < 10) sb.append('0');
        sb.append(f);
        return sb.toString();
    }
    private static String pad(String s, int w){
        if (s == null) s = "";
        if (s.length() >= w) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < w) sb.append(' ');
        return sb.toString();
    }
    private static String padRight(String s, int w) {
        if (s == null) s = "";
        if (s.length() >= w) return s;
        StringBuilder sb = new StringBuilder();
        int spaces = w - s.length();
        while (spaces-- > 0) sb.append(' ');
        sb.append(s);
        return sb.toString();
    }
    private static String trunc(String s, int max){
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
