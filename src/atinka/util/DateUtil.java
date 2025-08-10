package atinka.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Null-safe date parsing + helpers. */
public final class DateUtil {
    private DateUtil(){}

    /** Returns null on bad format. */
    public static LocalDate parseDateOrNull(String ymd) {
        try { return LocalDate.parse(ymd); } catch (Exception e) { return null; }
    }

    public static LocalDateTime dayStart(LocalDate d){ return d.atStartOfDay(); }
    public static LocalDateTime dayEnd(LocalDate d){ return d.atTime(23,59,59); }

    public static boolean isExpired(LocalDate expiry, LocalDate today) {
        if (expiry == null) return false; // treat missing expiry as non-expired
        return expiry.isBefore(today);
    }
}
