package bughunt.model.rules;

import bughunt.model.actors.Bug;
import bughunt.model.actors.Debugger;
import bughunt.model.core.Match;
import bughunt.model.pickups.Pickup;

/**
 * Detects when the Debugger shares a tile with a pickup or a bug and hands
 * the effect to that object itself, without any "if" per type.
 */
public class CollisionManager {

    public void check(Match match) {
        Debugger debugger = match.debugger();
        Pickup pickup = match.maze().cell(debugger.position()).pickup();
        if (pickup != null) {
            pickup.onCollected(match);
        }
        for (Bug bug : match.bugs()) {
            if (bug.position().equals(debugger.position())) {
                bug.onTouch(match);
            }
        }
    }
}
