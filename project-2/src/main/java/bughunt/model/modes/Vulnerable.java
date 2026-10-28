package bughunt.model.modes;

import bughunt.model.actors.Bug;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.core.Ticks;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;

/** After a breakpoint: the bug turns blue, slows down, wanders at random and can be captured. */
public class Vulnerable implements BugMode {
    private static final int TICKS_PER_FLASH = Ticks.of(0.2);

    @Override
    public Position target(Bug bug, Match match) {
        return null;
    }

    @Override
    public void onTouch(Bug bug, Match match) {
        int value = match.score().registerCapture();
        bug.switchTo(new Captured());
        match.events().publish(new GameEvent(EventType.BUG_CAPTURED, bug.position(), value));
    }

    @Override
    public double speed(Bug bug, Match match) {
        return match.config().vulnerableBugSpeed();
    }

    /** Flashes white during every other half-period while the breakpoint is ending. */
    @Override
    public Look look(Bug bug, Match match) {
        boolean white = match.modes().isVulnerableEnding()
                && (match.tickCount() / TICKS_PER_FLASH) % 2 == 0;
        return white ? Look.FLASHING : Look.VULNERABLE;
    }

    @Override
    public BugMode onBreakpointEnd(Bug bug, BugMode global) {
        return global;
    }

    @Override
    public String description() {
        return "vulnerable";
    }
}
