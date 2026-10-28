package bughunt.model.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bughunt.model.TestMatches;
import bughunt.model.core.Direction;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BugTargetTest {
    private Match match;

    @BeforeEach
    void setUp() {
        match = TestMatches.maze1Match();
        match.resetRound();
        assertEquals(new Position(13, 17), match.debugger().position());
        assertEquals(Direction.LEFT, match.debugger().direction());
    }

    private <T extends Bug> T bug(Class<T> type) {
        return match.bugs().stream().filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
    }

    @Test
    void nullPointerTargetsTheDebugger() {
        assertEquals(new Position(13, 17), bug(NullPointer.class).computeTarget(match));
    }

    @Test
    void raceConditionTargetsFourTilesAhead() {
        assertEquals(new Position(9, 17), bug(RaceCondition.class).computeTarget(match));
    }

    @Test
    void deadlockDoublesTheVectorFromNullPointer() {
        assertEquals(new Position(13, 8), bug(NullPointer.class).position());
        assertEquals(new Position(9, 26), bug(Deadlock.class).computeTarget(match));
    }

    @Test
    void heisenbugFleesWhenCloseAndChasesWhenFar() {
        Heisenbug heisenbug = bug(Heisenbug.class);
        assertEquals(new Position(16, 10), heisenbug.position());
        assertEquals(new Position(0, 24), heisenbug.computeTarget(match));

        match.debugger().setStart(new Position(1, 1));
        match.debugger().returnToStart();
        assertEquals(new Position(1, 1), heisenbug.computeTarget(match));
    }

    @Test
    void scatterCorners() {
        assertEquals(new Position(25, -2), bug(NullPointer.class).scatterCorner());
        assertEquals(new Position(2, -2), bug(RaceCondition.class).scatterCorner());
        assertEquals(new Position(27, 24), bug(Deadlock.class).scatterCorner());
        assertEquals(new Position(0, 24), bug(Heisenbug.class).scatterCorner());
    }
}
