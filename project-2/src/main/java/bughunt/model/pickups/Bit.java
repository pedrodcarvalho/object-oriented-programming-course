package bughunt.model.pickups;

import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.events.EventType;

/** A reviewed line of code: 10 points. */
public final class Bit extends Pickup {
    public static final int POINTS = 10;

    public Bit(Position position) {
        super(position, POINTS);
    }

    @Override
    public void onCollected(Match match) {
        collect(match, EventType.BIT_EATEN);
    }
}
