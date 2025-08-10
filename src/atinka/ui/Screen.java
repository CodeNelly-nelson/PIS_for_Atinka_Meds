package atinka.ui;

/** Base for all terminal screens. No GUI—just console flows. */
public abstract class Screen {
    /** Run this screen’s loop; return when the user chooses “Back”. */
    public abstract void run();
}
