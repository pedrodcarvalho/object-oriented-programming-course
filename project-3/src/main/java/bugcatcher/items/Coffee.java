package bugcatcher.items;

import bugcatcher.player.Player;
import javafx.scene.paint.Color;

public class Coffee extends FallingItem implements Scorable {
    public Coffee(double speed) {
        this("Coffee", speed);
    }

    protected Coffee(String name, double speed) {
        super(name, speed);
    }

    @Override
    public int getPoints() {
        return BASE_POINTS;
    }

    @Override
    public void onCaught(Player player) {
        player.addPoints(getPoints());
    }

    @Override
    protected Color getColor() {
        return Color.SADDLEBROWN;
    }

    @Override
    protected String getLabel() {
        return "+" + getPoints();
    }
}
