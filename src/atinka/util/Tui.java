package atinka.util;

/** Tui: tiny toast-like helpers for consistent feedback. */
public final class Tui {
    private Tui(){}

    public static void toastInfo(String msg){ System.out.println("[ i ] " + msg); }
    public static void toastSuccess(String msg){ System.out.println("[ ✓ ] " + msg); }
    public static void toastWarn(String msg){ System.out.println("[ ! ] " + msg); }
    public static void toastError(String msg){ System.out.println("[ x ] " + msg); }
}
