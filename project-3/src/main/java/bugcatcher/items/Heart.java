package bugcatcher.items;

import bugcatcher.player.Player;
import javafx.scene.paint.Color;

/**
 * A very rare extra life.
 * The class is final: nobody can extend Heart.
 */
public final class Heart extends FallingItem {

    public Heart(double speed) {
        super("Heart", speed);
    }

    @Override
    public void onCaught(Player player) {
        player.gainLife();
    }

    @Override
    protected Color getColor() {
        return Color.HOTPINK;
    }

    @Override
    protected String getLabel() {
        return "1UP";
    }
}
