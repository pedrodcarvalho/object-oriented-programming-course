package bughunt.model.modes;

import bughunt.model.actors.Bug;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.core.Ticks;

/** A captured bug: only its "eyes" run back to the backlog, and it comes out again shortly after. */
public class Captured implements BugMode {
    private static final int WAIT_IN_BACKLOG = Ticks.of(0.5);

    @Override
    public Position target(Bug bug, Match match) {
        return match.maze().backlogCenter();
    }

    @Override
    public BugMode update(Bug bug, Match match) {
        if (bug.position().equals(match.maze().backlogCenter())) {
            return new InBacklog(WAIT_IN_BACKLOG);
        }
        return this;
    }

    @Override
    public void onTouch(Bug bug, Match match) {
        // eyes do no harm to anyone
    }

    @Override
    public double speed(Bug bug, Match match) {
        return match.config().capturedBugSpeed();
    }

    @Override
    public Look look(Bug bug, Match match) {
        return Look.EYES;
    }

    @Override
    public boolean passesDoor() {
        return true;
    }

    @Override
    public boolean usesShortestPath() {
        return true;
    }

    @Override
    public boolean slowsInTunnel() {
        return false;
    }

    @Override
    public String description() {
        return "captured";
    }
}
