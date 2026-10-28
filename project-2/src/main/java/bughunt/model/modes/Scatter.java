package bughunt.model.modes;

import bughunt.model.actors.Bug;
import bughunt.model.core.Match;
import bughunt.model.core.Position;

/** Each bug heads back to its own corner of the maze: a breather for the player. */
public class Scatter extends HostileMode {

    @Override
    public Position target(Bug bug, Match match) {
        return bug.scatterCorner();
    }

    @Override
    public String description() {
        return "scatter";
    }
}
