package bughunt.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bughunt.model.TestMatches;
import bughunt.model.actors.Bug;
import bughunt.model.actors.Debugger;
import bughunt.model.actors.NullPointer;
import bughunt.model.events.EventType;
import bughunt.model.modes.Captured;
import bughunt.model.modes.Vulnerable;
import bughunt.model.rules.CollisionManager;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MatchTest {
    private Match match;

    @BeforeEach
    void setUp() {
        match = TestMatches.maze1Match();
    }

    private Bug nullPointer() {
        return match.bugs().stream().filter(b -> b instanceof NullPointer).findFirst().orElseThrow();
    }

    private void moveDebuggerTo(Position target) {
        Debugger debugger = match.debugger();
        debugger.setStart(target);
        debugger.returnToStart();
    }

    @Test
    void resetRoundAnnouncesTheRound() {
        AtomicInteger rounds = new AtomicInteger();
        match.events().subscribe(EventType.ROUND_STARTED, e -> rounds.incrementAndGet());
        match.resetRound();
        assertEquals(1, rounds.get());
    }

    @Test
    void debuggerMovesLeftForOneSecond() {
        assertEquals(new Position(13, 17), match.debugger().position());
        for (int i = 0; i < 60; i++) {
            match.tick();
        }
        Debugger debugger = match.debugger();
        assertEquals(new Position(7, 17), debugger.position());
        assertTrue(debugger.progress() >= 0.0 && debugger.progress() < 1.0, "progress " + debugger.progress());
        assertFalse(match.debuggerWasCaught());
    }

    @Test
    void touchingHostileBugCatchesTheDebuggerOnce() {
        AtomicInteger caught = new AtomicInteger();
        match.events().subscribe(EventType.DEBUGGER_CAUGHT, e -> caught.incrementAndGet());
        moveDebuggerTo(nullPointer().position());
        CollisionManager collisions = new CollisionManager();
        collisions.check(match);
        collisions.check(match);
        assertTrue(match.debuggerWasCaught());
        assertEquals(1, caught.get());
    }

    @Test
    void touchingVulnerableBugCapturesItAndFreezesBriefly() {
        Bug bug = nullPointer();
        match.modes().activateBreakpoint(match);
        assertInstanceOf(Vulnerable.class, bug.mode());
        moveDebuggerTo(bug.position());

        new CollisionManager().check(match);

        assertInstanceOf(Captured.class, bug.mode());
        assertEquals(200, match.score().score());
        assertFalse(match.debuggerWasCaught());
        for (int i = 0; i < 30; i++) {
            assertTrue(match.isFrozen(), "frozen at tick " + i);
            match.tick();
        }
        assertFalse(match.isFrozen());
    }
}
