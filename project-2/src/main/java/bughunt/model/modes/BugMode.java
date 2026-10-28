package bughunt.model.modes;

import bughunt.model.actors.Bug;
import bughunt.model.core.Match;
import bughunt.model.core.Position;

/**
 * A bug's state (State pattern). Each mode defines target, speed, look, what
 * happens on touching the Debugger and which mode comes next when something
 * changes. The default methods fit most modes.
 */
public interface BugMode {

    /** The tile the bug tries to reach; null = wander at random. */
    Position target(Bug bug, Match match);

    /** What happens when the bug and the Debugger share a tile. */
    void onTouch(Bug bug, Match match);

    /** Short text for the HUD. */
    String description();

    default double speed(Bug bug, Match match) {
        return match.config().bugSpeed();
    }

    default Look look(Bug bug, Match match) {
        return Look.NORMAL;
    }

    /** May the bug pass the door and walk inside the backlog? */
    default boolean passesDoor() {
        return false;
    }

    /** Follows the shortest path to the target instead of the "closest neighbour" rule? */
    default boolean usesShortestPath() {
        return false;
    }

    default boolean slowsInTunnel() {
        return true;
    }

    /** Called every tick; returns the next mode (or itself). */
    default BugMode update(Bug bug, Match match) {
        return this;
    }

    default BugMode onWaveChange(Bug bug, BugMode next) {
        return this;
    }

    default BugMode onBreakpointStart(Bug bug) {
        return this;
    }

    default BugMode onBreakpointEnd(Bug bug, BugMode global) {
        return this;
    }
}
