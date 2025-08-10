package atinka.ui;

import atinka.report.PerformanceReport;
import atinka.service.DrugService;
import atinka.util.ConsoleIO;

/** Reports screen — cancel-friendly menu. */
public final class ReportScreen extends Screen {
    private final DrugService drugs;
    public ReportScreen(DrugService drugs){ this.drugs = drugs; }

    @Override public void run() {
        boolean back=false;
        while(!back){
            ConsoleIO.clearScreen();
            ConsoleIO.printHeader("Reports");
            ConsoleIO.println("1) Generate Algorithm Performance Report");
            ConsoleIO.println("0) Back\n");
            int c = ConsoleIO.readIntInRange("Choose: ", 0, 1);
            switch (c) {
                case 1 -> generatePerformance();
                case 0 -> back = true;
            }
            if(!back) ConsoleIO.readLine("\nPress ENTER...");
        }
    }

    private void generatePerformance() {
        try {
            java.nio.file.Path p = PerformanceReport.generate(drugs);
            ConsoleIO.println("Report written to: " + p.toAbsolutePath());
        } catch (Exception ex) {
            ConsoleIO.println("Failed to generate report: " + ex.getMessage());
        }
    }
}
