package atinka.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Utilities for writing text reports into data/reports (atomic-ish write). */
public final class ReportsFS {
    private ReportsFS() {}

    /** Write the report content to a timestamped file under data/reports and return the path. */
    public static Path writeReport(String content) {
        try {
            // Ensure directories exist
            PathsFS.ensure();

            // Build filename: performance_YYYYMMDD_HHMMSS.txt
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "performance_" + ts + ".txt";
            Path dir = PathsFS.reportsDir();
            Path target = dir.resolve(filename);
            Path tmp = dir.resolve(filename + ".tmp");

            // Write to tmp, then move into place
            Files.writeString(tmp, content == null ? "" : content,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            Files.move(tmp, target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);

            return target;
        } catch (Exception e) {
            // Fallback: try a direct write to a simple name if anything failed above
            try {
                Path fallback = PathsFS.reportsDir().resolve("performance_fallback.txt");
                Files.writeString(fallback, content == null ? "" : content,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                return fallback;
            } catch (Exception ignored) { /* give up */ }
            return PathsFS.reportsDir().resolve("performance_error.txt");
        }
    }
}
