package atinka.util;

/** Toast-like helpers with color and icons. */
public final class Tui {
    private Tui(){}

    public static void toastInfo(String msg){
        if (Ansi.isEnabled())
            System.out.println(Ansi.fg("cyan") + "ⓘ  " + msg + Ansi.reset());
        else
            System.out.println("[ i ] " + msg);
    }

    public static void toastSuccess(String msg){
        if (Ansi.isEnabled())
            System.out.println(Ansi.fg("green") + "✔  " + msg + Ansi.reset());
        else
            System.out.println("[ ✓ ] " + msg);
    }

    public static void toastWarn(String msg){
        if (Ansi.isEnabled())
            System.out.println(Ansi.fg("yellow") + "⚠  " + msg + Ansi.reset());
        else
            System.out.println("[ ! ] " + msg);
    }

    public static void toastError(String msg){
        if (Ansi.isEnabled())
            System.out.println(Ansi.fg("red") + "✖  " + msg + Ansi.reset());
        else
            System.out.println("[ x ] " + msg);
    }
}
