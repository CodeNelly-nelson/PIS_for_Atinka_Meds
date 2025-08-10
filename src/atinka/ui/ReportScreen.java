package atinka.ui;

import atinka.report.PerformanceReport;
import atinka.report.SalesPeriodReport;
import atinka.service.DrugService;
import atinka.storage.ReportsFS;
import atinka.storage.SaleLogCsv;
import atinka.util.ConsoleIO;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReportScreen extends Screen {
    private final DrugService drugs;
    private final SaleLogCsv sales;

    public ReportScreen(DrugService drugs, SaleLogCsv sales){
        this.drugs = drugs;
        this.sales = sales;
    }

    @Override public void run() {
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Reports");
            ConsoleIO.println("1) Generate Algorithm Performance Report");
            ConsoleIO.println("2) View Performance Report");
            ConsoleIO.println("3) Generate Sales & Accounting Report");
            ConsoleIO.println("4) View Sales & Accounting Report");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 4);
            switch (c) {
                case 1 -> generatePerformance();
                case 2 -> viewPerformance();
                case 3 -> generateSalesPeriod();
                case 4 -> viewSales();
                case 0 -> back = true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void generatePerformance() {
        try {
            Path p = PerformanceReport.generate(drugs);
            ConsoleIO.println("Performance report written to: " + p.toAbsolutePath());
        } catch (Exception ex) {
            ConsoleIO.println("Failed to generate report: " + ex.getMessage());
        }
    }

    private void viewPerformance() {
        String txt = ReportsFS.readPerformance();
        if (txt == null || txt.trim().length()==0) {
            ConsoleIO.println("No performance report found. Generate one first.");
        } else {
            ConsoleIO.println("\n----- performance.txt -----\n");
            ConsoleIO.println(txt);
        }
    }

    private void generateSalesPeriod() {
        String fromS = ConsoleIO.readLineOrCancel("From date (YYYY-MM-DD)");
        if (fromS == null) { ConsoleIO.println("Cancelled."); return; }
        String toS   = ConsoleIO.readLineOrCancel("To date   (YYYY-MM-DD)");
        if (toS == null)   { ConsoleIO.println("Cancelled."); return; }
        try {
            LocalDate f = LocalDate.parse(fromS);
            LocalDate t = LocalDate.parse(toS);
            if (t.isBefore(f)) { ConsoleIO.println("Invalid: To date is before From date."); return; }
            LocalDateTime from = f.atStartOfDay();
            LocalDateTime to   = t.atTime(23,59,59);

            Path p = SalesPeriodReport.generate(drugs, sales, from, to);
            ConsoleIO.println("Sales report written to: " + p.toAbsolutePath());
        } catch (Exception ex) {
            ConsoleIO.println("Failed: " + ex.getMessage());
        }
    }

    private void viewSales() {
        String txt = ReportsFS.readSales();
        if (txt == null || txt.trim().length()==0) {
            ConsoleIO.println("No sales report found. Generate one first.");
        } else {
            ConsoleIO.println("\n----- sales.txt -----\n");
            ConsoleIO.println(txt);
        }
    }
}
