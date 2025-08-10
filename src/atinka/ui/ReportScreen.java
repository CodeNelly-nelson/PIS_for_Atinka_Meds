package atinka.ui;

import atinka.report.PerformanceReport;
import atinka.service.DrugService;
import atinka.util.ConsoleIO;

public final class ReportScreen extends Screen {
    private final DrugService drugs;
    public ReportScreen(DrugService drugs){ this.drugs = drugs; }

    @Override public void run() {
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader("Reports");
        try {
            java.nio.file.Path p = PerformanceReport.generate(drugs);
            ConsoleIO.println("Report written to: " + p.toAbsolutePath());
        } catch (Exception ex) {
            ConsoleIO.println("Failed to generate report: " + ex.getMessage());
        }
        ConsoleIO.readLine("\nPress ENTER...");
    }
}

