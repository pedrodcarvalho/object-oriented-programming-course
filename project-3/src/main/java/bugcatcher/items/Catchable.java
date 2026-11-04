package bugcatcher.items;

import bugcatcher.player.Player;

/**
 * Anything the player can catch with the basket.
 * Each kind of item decides for itself what happens when it is caught.
 */
public interface Catchable {

    void onCaught(Player player);
}
