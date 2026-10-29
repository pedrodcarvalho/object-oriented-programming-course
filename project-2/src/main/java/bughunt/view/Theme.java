package bughunt.view;

import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The "neon circuit board" look in one place: layout constants, the palette and the
 * fonts. No other class hard-codes a size, a colour or a font family.
 */
public final class Theme {
    /** Size of one maze tile, in pixels. */
    public static final int TILE = 24;
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    /** Top-left corner of the maze inside the window. */
    public static final int MAZE_X = 104;
    public static final int MAZE_Y = 84;
    /** The HUD panel on the right. */
    public static final int HUD_X = 900;
    public static final int HUD_WIDTH = 340;

    public static final Color BACKGROUND = Color.web("#07130d");
    public static final Color GRID = Color.web("#0f2619");
    public static final Color TRACE = Color.web("#2bff88");
    public static final Color DOOR = Color.web("#ff2bd6");
    public static final Color BIT = Color.web("#fff1d0");
    public static final Color BREAKPOINT = Color.web("#ff3b3b");
    public static final Color COFFEE = Color.web("#8b5a2b");
    public static final Color COFFEE_SURFACE = Color.web("#d9a066");
    public static final Color DEBUGGER = Color.web("#ffe53b");
    public static final Color NULL_POINTER = Color.web("#ff3b3b");
    public static final Color RACE_CONDITION = Color.web("#ff8ad8");
    public static final Color DEADLOCK = Color.web("#3be8ff");
    public static final Color HEISENBUG = Color.web("#ffa53b");
    public static final Color VULNERABLE = Color.web("#3b6bff");
    public static final Color FLASH = Color.WHITE;
    public static final Color PUPIL = Color.web("#1a2a80");
    public static final Color CRASH = Color.web("#ff3b3b");
    public static final Color CRASH_GHOST = Color.web("#3be8ff");

    /** Preferred monospace families, best first; "Monospaced" is JavaFX's logical fallback. */
    private static final List<String> MONOSPACE_FAMILIES =
            List.of("JetBrains Mono", "Fira Code", "DejaVu Sans Mono", "Consolas", "Menlo", "Courier New");
    private static final String FALLBACK_FAMILY = "Monospaced";

    private static String family;

    private Theme() {
    }

    /** Bold monospace, for banners and headings. */
    public static Font title(double size) {
        return Font.font(family(), FontWeight.BOLD, size);
    }

    /** Regular monospace, for body text and numbers. */
    public static Font body(double size) {
        return Font.font(family(), FontWeight.NORMAL, size);
    }

    /** The first preferred family installed on this machine (looked up once, on first use). */
    private static synchronized String family() {
        if (family == null) {
            List<String> installed = Font.getFamilies();
            family = MONOSPACE_FAMILIES.stream()
                    .filter(installed::contains)
                    .findFirst()
                    .orElse(FALLBACK_FAMILY);
        }
        return family;
    }
}
