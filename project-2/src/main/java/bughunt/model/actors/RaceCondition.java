package bughunt.model.actors;

import bughunt.model.core.Match;
import bughunt.model.core.Position;

/** Races to get there first: aims 4 tiles ahead of the Debugger. */
public final class RaceCondition extends Bug {
    private static final int TILES_AHEAD = 4;

    public RaceCondition(Position start, Position scatterCorner) {
        super("RaceCondition", start, scatterCorner, 1);
    }

    @Override
    public Position computeTarget(Match match) {
        Debugger debugger = match.debugger();
        return debugger.position().move(debugger.direction(), TILES_AHEAD);
    }
}
