package atinka.ui;

import atinka.util.ConsoleIO;

/** Minimal text menu (no java.util; uses arrays). */
public final class Menu {
    private final String title;
    private final MenuItem[] items; // last item should be “Back/Exit”

    public Menu(String title, MenuItem[] items) {
        this.title = title;
        this.items = items;
    }

    /** Render once and invoke selected item. Caller decides loop/back. */
    public int showOnceAndDispatch() {
        ConsoleIO.clearScreen();
        ConsoleIO.printHeader(title);
        for (int i = 0; i < items.length; i++) {
            ConsoleIO.println((i + 1) + ") " + items[i].label());
        }
        ConsoleIO.println("0) Back\n");
        int choice = ConsoleIO.readIntInRange("Choose: ", 0, items.length);
        if (choice == 0) return 0;
        items[choice - 1].run();
        return choice;
    }
}
