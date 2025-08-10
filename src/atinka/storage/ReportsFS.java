package atinka.storage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Report file utilities: single-file per type, atomic overwrite and read. */
public final class ReportsFS {
    private ReportsFS() {}

    public static Path performancePath() { return PathsFS.reportsDir().resolve("performance.txt"); }
    public static Path salesPath()       { return PathsFS.reportsDir().resolve("sales.txt"); }

    private static void ensureDirs() { PathsFS.ensure(); }

    public static Path writePerformance(String content) {
        return writeSingleton(performancePath(), content);
    }

    public static Path writeSales(String content) {
        return writeSingleton(salesPath(), content);
    }

    public static String readPerformance() {
        return readAllOrNull(performancePath());
    }

    public static String readSales() {
        return readAllOrNull(salesPath());
    }

    private static Path writeSingleton(Path target, String content) {
        ensureDirs();
        Path tmp = target.resolveSibling(target.getFileName().toString() + ".tmp");
        try {
            Files.write(tmp, (content == null ? "" : content).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            Files.move(tmp, target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            return target;
        } catch (Exception e) {
            try {
                Files.write(target, (content == null ? "" : content).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (Exception ignored) {}
            return target;
        }
    }

    private static String readAllOrNull(Path p) {
        try {
            if (!Files.exists(p)) return null;
            byte[] b = Files.readAllBytes(p);
            return new String(b, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
