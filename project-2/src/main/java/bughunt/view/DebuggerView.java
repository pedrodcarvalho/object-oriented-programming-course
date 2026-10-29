package bughunt.view;

import bughunt.model.core.Direction;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;

/**
 * The player: a yellow arrow cursor pointing along its direction of travel. When the
 * debugger crashes (life lost) the cursor is replaced by a glitched "X".
 */
final class DebuggerView {
    private static final double CROSS = 8;
    private static final double GLITCH_OFFSET = 2;

    private final Group root = new Group();
    /** Drawn pointing right, symmetric around (0, 0) so rotation pivots on the tile centre. */
    private final Polygon cursor = new Polygon(
            9, 0,
            -9, -9,
            -4, 0,
            -9, 9);
    private final Group crash = new Group();

    DebuggerView() {
        cursor.setFill(Theme.DEBUGGER);
        // A ghost copy offset in cyan behind the red cross gives the "glitch" look.
        crash.getChildren().addAll(
                cross(GLITCH_OFFSET, Theme.CRASH_GHOST),
                cross(0, Theme.CRASH));
        crash.setVisible(false);
        root.getChildren().addAll(cursor, crash);
    }

    Node node() {
        return root;
    }

    /**
     * Places the cursor with its centre at (x, y) px, facing {@code facing} plus
     * {@code extraQuarterTurns} clockwise quarter turns (the death spin).
     */
    void showCursor(double x, double y, Direction facing, int extraQuarterTurns) {
        root.setVisible(true);
        root.setTranslateX(x);
        root.setTranslateY(y);
        cursor.setRotate(angle(facing) + 90.0 * extraQuarterTurns);
        cursor.setVisible(true);
        crash.setVisible(false);
    }

    /** Replaces the cursor with the glitched crash cross, centred at (x, y) px. */
    void showCrash(double x, double y) {
        root.setVisible(true);
        root.setTranslateX(x);
        root.setTranslateY(y);
        cursor.setVisible(false);
        crash.setVisible(true);
    }

    void hide() {
        root.setVisible(false);
    }

    /** Rotation in degrees for a shape drawn pointing right (JavaFX rotates clockwise, y down). */
    static double angle(Direction direction) {
        return switch (direction) {
            case RIGHT -> 0;
            case DOWN -> 90;
            case LEFT -> 180;
            case UP -> 270;
        };
    }

    private static Group cross(double offset, Color colour) {
        Line a = new Line(-CROSS + offset, -CROSS, CROSS + offset, CROSS);
        Line b = new Line(-CROSS + offset, CROSS, CROSS + offset, -CROSS);
        for (Line line : new Line[] {a, b}) {
            line.setStroke(colour);
            line.setStrokeWidth(3);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
        }
        return new Group(a, b);
    }
}
