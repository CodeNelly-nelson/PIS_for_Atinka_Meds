package atinka.storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ReportsFS {
    public static Path writeReport(String content) {
        try {
            PathsFS.ensure();
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path file = PathsFS.REPORTS_DIR.resolve("performance_" + stamp + ".txt");
            try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                bw.write(content);
            }
            return file;
        } catch (IOException e) { throw new RuntimeException("Failed to write report", e); }
    }
}