package bugcatcher.items;

import bugcatcher.player.Player;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public abstract class FallingItem implements Catchable {
    public static final double SIZE = 44;

    private static int createdCount = 0;

    private final String name;
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

    protected abstract Color getColor();

    protected abstract String getLabel();

    public final Circle createShape() {
        double radius = SIZE / 2;
        return new Circle(radius, radius, radius, getColor());
    }

    public final Text createLabel() {
        Text text = new Text(getLabel());
        text.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        text.setFill(Color.WHITE);
        double width = text.getLayoutBounds().getWidth();
        text.setTranslateX((SIZE - width) / 2);
        text.setTranslateY(SIZE / 2 + 5);
        return text;
    }

    @Override
    public String toString() {
        return String.format("%s (%.0f px/s)", name, speed);
    }

    @Override
    public abstract void onCaught(Player player);
}
