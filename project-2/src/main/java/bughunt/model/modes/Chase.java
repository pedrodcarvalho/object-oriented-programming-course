package bughunt.model.modes;

import bughunt.model.actors.Bug;
import bughunt.model.core.Match;
import bughunt.model.core.Position;

/** Each bug chases the Debugger with its own personality (polymorphism in computeTarget). */
public class Chase extends HostileMode {

    @Override
    public Position target(Bug bug, Match match) {
        return bug.computeTarget(match);
    }

    @Override
    public String description() {
        return "chase";
    }
}
