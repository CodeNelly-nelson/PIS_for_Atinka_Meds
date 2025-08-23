package atinka.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Atomic write helpers for report files. */
public final class ReportsFS {
    private ReportsFS(){}

    public static Path writeReport(String fileName, String content){
        Path out = PathsFS.reportPath(fileName);
        Path tmp = out.resolveSibling(out.getFileName().toString() + ".tmp");
        try {
            byte[] bytes = content.getBytes("UTF-8");
            Files.write(tmp, bytes);
            Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return out;
        } catch (Exception e){
            // Fallback: non-atomic move
            try {
                Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ignored) {}
            return out;
        }
    }

    public static String readReport(String fileName){
        Path p = PathsFS.reportPath(fileName);
        try {
            if (!Files.exists(p)) return "";
            byte[] b = Files.readAllBytes(p);
            return new String(b, "UTF-8");
        } catch (Exception e){
            return "";
        }
    }
}
