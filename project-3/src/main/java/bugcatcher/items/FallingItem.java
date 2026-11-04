package bugcatcher.items;

import bugcatcher.player.Player;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Base class of everything that falls from the top of the screen.
 *
 * It is abstract because a "generic falling item" makes no sense in the game:
 * only concrete kinds (Coffee, Bug, Heart...) are ever created.
 */
public abstract class FallingItem implements Catchable {

    /** Diameter of every item, in pixels. */
    public static final double SIZE = 44;

    /** Class variable: shared by all items, counts how many were created. */
    private static int createdCount = 0;

    private final String name;
    /** Falling speed in pixels per second. Protected so subclasses can read it directly. */
    protected final double speed;

    protected FallingItem(String name, double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be positive, got " + speed);
        }
        this.name = name;
        this.speed = speed;
        createdCount++;
    }

    public String getName() {
        return name;
    }

    public double getSpeed() {
        return speed;
    }

    public static int getCreatedCount() {
        return createdCount;
    }

    /** Each subclass chooses its own color... */
    protected abstract Color getColor();

    /** ...and the short text drawn on top of it. */
    protected abstract String getLabel();

    /**
     * The circle FXGL uses to draw the item (and to detect collisions).
     * It is final: every item is drawn the same way, only color and label change.
     */
    public final Circle createShape() {
        double radius = SIZE / 2;
        return new Circle(radius, radius, radius, getColor());
    }

    /** The label centered on top of the circle. */
    public final Text createLabel() {
        Text text = new Text(getLabel());
        text.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        text.setFill(Color.WHITE);
        double width = text.getLayoutBounds().getWidth();
        text.setTranslateX((SIZE - width) / 2);
        text.setTranslateY(SIZE / 2 + 5);
        return text;
    }

    /** Overrides Object.toString(). */
    @Override
    public String toString() {
        return String.format("%s (%.0f px/s)", name, speed);
    }

    /** Still abstract here: each subclass must say what happens when it is caught. */
    @Override
    public abstract void onCaught(Player player);
}
