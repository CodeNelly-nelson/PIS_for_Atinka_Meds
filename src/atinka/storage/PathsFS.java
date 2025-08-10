package atinka.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Centralizes filesystem locations for data persistence. */
public final class PathsFS {
    private PathsFS() {}

    public static Path dataDir() {
        return Paths.get("data");
    }

    public static Path drugsPath() {
        return dataDir().resolve("drugs.csv");
    }

    public static Path suppliersPath() {
        return dataDir().resolve("suppliers.csv");
    }

    public static Path customersPath() {
        return dataDir().resolve("customers.csv");
    }

    public static Path purchaseLogPath() {
        return dataDir().resolve("purchases.csv");
    }

    public static Path salesLogPath() {
        return dataDir().resolve("sales.csv");
    }

    public static Path reportsDir() {
        return dataDir().resolve("reports");
    }

    public static void ensure() {
        try {
            Files.createDirectories(dataDir());
            Files.createDirectories(reportsDir());
        } catch (Exception e) {
            throw new RuntimeException("Unable to create data directories: " + e.getMessage(), e);
        }
    }
}
