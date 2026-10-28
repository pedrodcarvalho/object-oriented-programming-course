package bughunt.model.actors;

import bughunt.model.core.Match;
import bughunt.model.core.Position;

/** Points straight at you: the target is the Debugger's tile. */
public final class NullPointer extends Bug {

    public NullPointer(Position start, Position scatterCorner) {
        super("NullPointer", start, scatterCorner, 0);
    }

    @Override
    public Position computeTarget(Match match) {
        return match.debugger().position();
    }
}
