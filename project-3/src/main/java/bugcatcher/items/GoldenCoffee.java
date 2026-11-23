package bugcatcher.items;

import javafx.scene.paint.Color;

public class GoldenCoffee extends Coffee {
    private static final int MULTIPLIER = 5;

    public GoldenCoffee(double speed) {
        super("Golden Coffee", speed * 1.5);
    }

    @Override
    public int getPoints() {
        return super.getPoints() * MULTIPLIER;
    }

    @Override
    protected Color getColor() {
        return Color.GOLDENROD;
    }
}
