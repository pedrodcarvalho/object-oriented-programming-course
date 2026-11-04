package bugcatcher.items;

import bugcatcher.player.Player;
import javafx.scene.paint.Color;

/** A bug in the code! Catching it costs a life, so the player must dodge it. */
public class Bug extends FallingItem implements Dangerous {

    public Bug(double speed) {
        super("Bug", speed);
    }

    @Override
    public int getDamage() {
        return 1;
    }

    @Override
    public void onCaught(Player player) {
        player.loseLife(getDamage());
    }

    @Override
    protected Color getColor() {
        return Color.CRIMSON;
    }

    @Override
    protected String getLabel() {
        return "BUG";
    }
}
