package atinka.ui;

/** Single menu entry + action (Command pattern). */
public interface MenuItem {
    String label();
    void run();
}
