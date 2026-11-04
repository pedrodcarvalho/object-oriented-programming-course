package bugcatcher.items;

import bugcatcher.player.Player;
import javafx.scene.paint.Color;

/** A cup of coffee: every programmer's fuel. Worth points when caught. */
public class Coffee extends FallingItem implements Scorable {

    public Coffee(double speed) {
        this("Coffee", speed); // this(...) calls the other constructor below
    }

    /** Used by subclasses (like GoldenCoffee) that want a different name. */
    protected Coffee(String name, double speed) {
        super(name, speed); // super(...) calls the FallingItem constructor
    }

    @Override
    public int getPoints() {
        return BASE_POINTS; // constant inherited from the Scorable interface
    }

    @Override
    public void onCaught(Player player) {
        // getPoints() is chosen at runtime (dynamic binding):
        // for a GoldenCoffee this calls GoldenCoffee.getPoints().
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
