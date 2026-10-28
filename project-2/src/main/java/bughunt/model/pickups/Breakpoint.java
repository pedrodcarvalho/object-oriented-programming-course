package bughunt.model.pickups;

import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.events.EventType;

/** 50 points, and it makes the bugs vulnerable for a few seconds. */
public final class Breakpoint extends Pickup {
    public static final int POINTS = 50;

    public Breakpoint(Position position) {
        super(position, POINTS);
    }

    @Override
    public void onCollected(Match match) {
        collect(match, EventType.BREAKPOINT_EATEN);
    }
}
