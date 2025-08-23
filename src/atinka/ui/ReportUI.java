package atinka.ui;

import atinka.report.PerformanceReport;
import atinka.report.SalesPeriodReport;
import atinka.storage.ReportsFS;
import atinka.util.ConsoleIO;
import atinka.util.Tui;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReportUI {

    public void show(){
        while (true){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Reports");
            ConsoleIO.println("1) Generate Performance Report");
            ConsoleIO.println("2) Generate Sales Report (period)");
            ConsoleIO.println("3) View last Performance Report");
            ConsoleIO.println("4) View last Sales Report");
            ConsoleIO.println("0) Back");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 4);
            if (c == 0) return;

            try {
                switch (c){
                    case 1: genPerf(); break;
                    case 2: genSales(); break;
                    case 3: viewPerf(); break;
                    case 4: viewSales(); break;
                }
            } catch (Exception e){
                Tui.toastError("Error: " + e.getMessage());
                pause();
            }
        }
    }

    // Dependencies are fetched lazily from Main via static holders (set by Main)
    public static atinka.service.DrugService DRUGS;
    public static atinka.storage.SaleLogCsv SALES;

    private void genPerf(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Generate Performance Report");
        java.nio.file.Path p = PerformanceReport.generate(DRUGS);
        Tui.toastSuccess("Generated: " + p.toString());
        pause();
    }

    private void genSales(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Generate Sales Report (period)");
        String fromD = ConsoleIO.readLine("From DATE [YYYY-MM-DD] (0=Cancel): ");
        if (isCancel(fromD)) return;
        String toD = ConsoleIO.readLine("To DATE [YYYY-MM-DD] (0=Cancel): ");
        if (isCancel(toD)) return;

        LocalDateTime from = toDateStart(fromD);
        LocalDateTime to = toDateEnd(toD);

        java.nio.file.Path p = SalesPeriodReport.generate(from, to, SALES, DRUGS);
        Tui.toastSuccess("Generated: " + p.toString());
        pause();
    }

    private void viewPerf(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Performance Report (last)");
        String s = ReportsFS.readReport("performance.txt");
        if (s == null || s.trim().length()==0) Tui.toastInfo("No report yet.");
        else ConsoleIO.println(s);
        pause();
    }

    private void viewSales(){
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Sales Report (last)");
        String s = ReportsFS.readReport("sales_period.txt");
        if (s == null || s.trim().length()==0) Tui.toastInfo("No report yet.");
        else ConsoleIO.println(s);
        pause();
    }

    private boolean isCancel(String s){
        if (s == null) return true;
        s = s.trim();
        return s.equals("0") || s.equalsIgnoreCase("c") || s.equalsIgnoreCase("cancel");
    }

    private LocalDateTime toDateStart(String s){
        try {
            LocalDate d = LocalDate.parse(s.trim());
            return d.atStartOfDay();
        } catch (Exception e){
            return LocalDateTime.MIN.plusYears(1);
        }
    }
    private LocalDateTime toDateEnd(String s){
        try {
            LocalDate d = LocalDate.parse(s.trim());
            return d.atTime(23,59,59);
        } catch (Exception e){
            return LocalDateTime.MAX.minusYears(1);
        }
    }

    private void pause(){ ConsoleIO.readLine("Press Enter to continue..."); }
}
