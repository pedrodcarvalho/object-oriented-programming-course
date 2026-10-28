package bughunt.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import bughunt.model.TestMatches;
import bughunt.model.actors.Bug;
import bughunt.model.actors.NullPointer;
import bughunt.model.core.Direction;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.modes.Chase;
import bughunt.model.modes.Scatter;
import bughunt.model.modes.Vulnerable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModeControllerTest {
    private Match match;
    private ModeController modes;

    @BeforeEach
    void setUp() {
        match = TestMatches.maze1Match();
        modes = match.modes();
    }

    private void update(int times) {
        for (int i = 0; i < times; i++) {
            modes.update(match);
        }
    }

    private Bug nullPointer() {
        return match.bugs().stream().filter(b -> b instanceof NullPointer).findFirst().orElseThrow();
    }

    /** Updates only this bug until it takes one step. */
    private static void stepOnce(Bug bug, Match match) {
        Position before = bug.position();
        for (int i = 0; i < 60 && bug.position().equals(before); i++) {
            bug.update(match);
        }
    }

    @Test
    void firstWaveIsScatterThenChase() {
        assertInstanceOf(Scatter.class, modes.newGlobalMode());
        assertEquals("SCATTER", modes.waveName());
        update(420);
        assertInstanceOf(Chase.class, modes.newGlobalMode());
        assertEquals("CHASE", modes.waveName());
    }

    @Test
    void withoutWaveChangeNullPointerKeepsGoingLeft() {
        Bug bug = nullPointer();
        stepOnce(bug, match);
        assertEquals(Direction.LEFT, bug.direction());
        assertEquals(new Position(12, 8), bug.position());
    }

    @Test
    void waveChangeMakesBugsOutsideTheBacklogReverse() {
        Bug bug = nullPointer();
        assertEquals(Direction.LEFT, bug.direction());
        update(420);
        assertInstanceOf(Chase.class, bug.mode());
        stepOnce(bug, match);
        assertEquals(Direction.RIGHT, bug.direction());
        assertEquals(new Position(14, 8), bug.position());
    }

    @Test
    void waveClockStopsDuringBreakpoint() {
        update(400);
        modes.activateBreakpoint(match);
        update(360);
        assertEquals("SCATTER", modes.waveName());
        assertInstanceOf(Scatter.class, modes.newGlobalMode());
        update(20);
        assertEquals("CHASE", modes.waveName());
        assertInstanceOf(Chase.class, modes.newGlobalMode());
    }

    @Test
    void breakpointLastsSixSecondsAtLevelOne() {
        assertEquals(360, modes.breakpointDurationTicks());
    }

    @Test
    void vulnerableBugsReturnToGlobalModeWhenBreakpointEnds() {
        Bug bug = nullPointer();
        modes.activateBreakpoint(match);
        assertInstanceOf(Vulnerable.class, bug.mode());
        update(359);
        assertInstanceOf(Vulnerable.class, bug.mode());
        update(1);
        assertInstanceOf(Scatter.class, bug.mode());
    }
}
