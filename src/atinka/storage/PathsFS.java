package atinka.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Central file-system paths + bootstrap for data directory. */
public final class PathsFS {
    private PathsFS(){}

    // Root data dir: ./data
    public static Path dataRoot() { return Path.of("data"); }

    // Leaf files/dirs
    public static Path drugsPath()        { return dataRoot().resolve("drugs.csv"); }
    public static Path suppliersPath()    { return dataRoot().resolve("suppliers.csv"); }
    public static Path customersPath()    { return dataRoot().resolve("customers.csv"); }
    public static Path purchaseLogPath()  { return dataRoot().resolve("purchase_log.csv"); }
    public static Path salesLogPath()     { return dataRoot().resolve("sales_log.csv"); }
    public static Path reportsDir()       { return dataRoot().resolve("reports"); }

    /** Ensure folders exist and the core CSV files are present. */
    public static void ensure() {
        try {
            // directories
            if (!Files.exists(dataRoot()))  Files.createDirectories(dataRoot());
            if (!Files.exists(reportsDir())) Files.createDirectories(reportsDir());

            // files (touch if missing)
            touchIfMissing(drugsPath());
            touchIfMissing(suppliersPath());
            touchIfMissing(customersPath());
            touchIfMissing(purchaseLogPath());
            touchIfMissing(salesLogPath());
        } catch (Exception e) {
            System.out.println("[WARN] Failed to ensure data directories/files: " + e.getMessage());
        }
    }

    private static void touchIfMissing(Path p) {
        try {
            if (!Files.exists(p)) {
                Files.writeString(p, "", StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
        } catch (Exception e) {
            System.out.println("[WARN] Failed to create " + p + ": " + e.getMessage());
        }
    }
}
