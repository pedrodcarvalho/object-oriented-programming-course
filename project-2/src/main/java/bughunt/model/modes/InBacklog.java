package bughunt.model.modes;

import bughunt.model.actors.Bug;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;

/**
 * Waiting in the backlog (the central pen). While waiting, the bug wanders inside;
 * when the wait is over it leaves through the door and takes the match's global mode.
 */
public class InBacklog implements BugMode {
    private int wait;

    public InBacklog(int waitInTicks) {
        this.wait = waitInTicks;
    }

    @Override
    public BugMode update(Bug bug, Match match) {
        if (wait > 0) {
            wait--;
            return this;
        }
        if (bug.position().equals(match.maze().backlogExit())) {
            match.events().publish(new GameEvent(EventType.BUG_RELEASED, bug.position(), 0));
            return match.modes().newGlobalMode();
        }
        return this;
    }

    @Override
    public Position target(Bug bug, Match match) {
        return wait > 0 ? null : match.maze().backlogExit();
    }

    @Override
    public void onTouch(Bug bug, Match match) {
        // inside the backlog the bug cannot reach the Debugger
    }

    @Override
    public double speed(Bug bug, Match match) {
        return match.config().backlogSpeed();
    }

    @Override
    public boolean passesDoor() {
        return wait <= 0;
    }

    @Override
    public boolean usesShortestPath() {
        return true;
    }

    @Override
    public String description() {
        return wait > 0 ? "in backlog" : "leaving";
    }
}
