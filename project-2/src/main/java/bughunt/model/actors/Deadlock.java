package bughunt.model.actors;

import bughunt.model.core.Match;
import bughunt.model.core.Position;

/**
 * Depends on another bug: takes the vector from NullPointer to the tile 2 ahead
 * of the Debugger and doubles it, closing the trap from the opposite side.
 */
public final class Deadlock extends Bug {
    private static final int TILES_AHEAD = 2;
    private final NullPointer partner;

    public Deadlock(Position start, Position scatterCorner, NullPointer partner) {
        super("Deadlock", start, scatterCorner, 2);
        this.partner = partner;
    }

    @Override
    public Position computeTarget(Match match) {
        Debugger debugger = match.debugger();
        Position pivot = debugger.position().move(debugger.direction(), TILES_AHEAD);
        Position origin = partner.position();
        return new Position(2 * pivot.x() - origin.x(), 2 * pivot.y() - origin.y());
    }
}
