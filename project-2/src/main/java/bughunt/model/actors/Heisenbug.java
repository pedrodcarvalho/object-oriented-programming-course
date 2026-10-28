package bughunt.model.actors;

import bughunt.model.core.Match;
import bughunt.model.core.Position;

/** Vanishes when observed up close: chases from afar and flees to its corner within 8 tiles. */
public final class Heisenbug extends Bug {
    private static final double OBSERVATION_DISTANCE = 8;

    public Heisenbug(Position start, Position scatterCorner) {
        super("Heisenbug", start, scatterCorner, 3);
    }

    @Override
    public Position computeTarget(Match match) {
        Position debugger = match.debugger().position();
        return position.distanceTo(debugger) > OBSERVATION_DISTANCE ? debugger : scatterCorner();
    }
}
