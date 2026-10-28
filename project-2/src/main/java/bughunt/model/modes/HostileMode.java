package bughunt.model.modes;

import bughunt.model.actors.Bug;
import bughunt.model.core.Match;

/** Base of the dangerous modes (Scatter and Chase): touching the bug costs a life. */
public abstract class HostileMode implements BugMode {

    @Override
    public void onTouch(Bug bug, Match match) {
        match.captureDebugger();
    }

    @Override
    public BugMode onWaveChange(Bug bug, BugMode next) {
        bug.reverseDirection();
        return next;
    }

    @Override
    public BugMode onBreakpointStart(Bug bug) {
        bug.reverseDirection();
        return new Vulnerable();
    }
}
