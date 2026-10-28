package bughunt.model.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bughunt.model.actors.Debugger;
import bughunt.model.core.Direction;
import bughunt.model.core.Position;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;
import bughunt.model.maze.InvalidMazeException;
import bughunt.model.maze.MazeLibrary;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MatchSessionTest {
    private MazeLibrary library;
    private MatchSession session;

    @BeforeEach
    void setUp() throws InvalidMazeException {
        library = MazeLibrary.fromClasspath();
        session = new MatchSession(library, 0);
    }

    private void tick(int times) {
        for (int i = 0; i < times; i++) {
            session.tick();
        }
    }

    private void reachPlaying() {
        tick(Ready.DURATION);
        assertEquals(PhaseKind.PLAYING, session.phase().kind());
    }

    @Test
    void newSessionIsReadyAtLevelOne() {
        assertEquals(PhaseKind.READY, session.phase().kind());
        assertEquals(0, session.phase().elapsedTicks());
        assertFalse(session.isOver());
        assertEquals(1, session.match().levelNumber());
        assertEquals(0, session.match().score().score());
        assertEquals(3, session.match().debugger().lives());
        assertEquals(28, session.match().maze().width());
    }

    @Test
    void readyLastsTwoSecondsWithEverythingStill() {
        Position start = session.match().debugger().position();
        tick(119);
        assertEquals(PhaseKind.READY, session.phase().kind());
        assertEquals(119, session.phase().elapsedTicks());
        assertEquals(start, session.match().debugger().position());
        assertEquals(0, session.match().tickCount());
        session.tick();
        assertEquals(PhaseKind.PLAYING, session.phase().kind());
    }

    @Test
    void directionRequestedDuringReadyIsTakenOncePlaying() {
        Debugger debugger = session.match().debugger();
        assertEquals(Direction.LEFT, debugger.direction());
        session.requestDirection(Direction.UP);
        reachPlaying();

        // At the start tile UP is a wall, so the first possible turn is one tile to the left.
        for (int i = 0; i < 60 && debugger.direction() == Direction.LEFT; i++) {
            session.tick();
        }
        assertEquals(Direction.UP, debugger.direction());
    }

    @Test
    void caughtDebuggerLosesALifeAndTheRoundRestarts() {
        reachPlaying();
        session.match().captureDebugger();
        session.tick();
        assertEquals(PhaseKind.LIFE_LOST, session.phase().kind());
        assertEquals(3, session.match().debugger().lives());

        tick(LifeLost.DURATION - 1);
        assertEquals(PhaseKind.LIFE_LOST, session.phase().kind());
        session.tick();
        assertEquals(PhaseKind.READY, session.phase().kind());
        assertEquals(2, session.match().debugger().lives());
        assertFalse(session.match().debuggerWasCaught());
        assertEquals(session.match().debugger().start(), session.match().debugger().position());
    }

    @Test
    void losingTheLastLifeEndsTheMatchAndAnnouncesTheScoreOnce() {
        List<GameEvent> gameOvers = new ArrayList<>();
        session.match().events().subscribe(EventType.GAME_OVER, gameOvers::add);
        Debugger debugger = session.match().debugger();
        debugger.loseLife();
        debugger.loseLife();
        session.match().score().add(1234);

        reachPlaying();
        session.match().captureDebugger();
        session.tick();
        assertEquals(PhaseKind.LIFE_LOST, session.phase().kind());
        tick(LifeLost.DURATION);

        assertEquals(PhaseKind.GAME_OVER, session.phase().kind());
        assertTrue(session.isOver());
        assertEquals(0, debugger.lives());
        tick(300);
        assertEquals(PhaseKind.GAME_OVER, session.phase().kind());
        assertEquals(1, gameOvers.size());
        assertEquals(session.match().score().score(), gameOvers.get(0).value());
        assertEquals(1234, gameOvers.get(0).value());
    }

    @Test
    void clearedLevelFlashesTheMazeThenStartsTheNextLevel() {
        reachPlaying();
        session.match().markLevelCleared();
        session.tick();
        assertEquals(PhaseKind.LEVEL_CLEARED, session.phase().kind());
        LevelCleared cleared = assertInstanceOf(LevelCleared.class, session.phase());

        tick(30);
        assertEquals(30, cleared.elapsedTicks());
        assertFalse(cleared.mazeFlashOn());
        tick(42);
        assertEquals(72, cleared.elapsedTicks());
        assertTrue(cleared.mazeFlashOn());

        tick(LevelCleared.DURATION - 72);
        assertEquals(PhaseKind.READY, session.phase().kind());
        assertEquals(2, session.match().levelNumber());
        assertFalse(session.match().levelWasCleared());
    }

    @Test
    void mazeStaysStillForTheFirstSecondThenFlashesEveryFifthOfASecond() {
        reachPlaying();
        session.match().markLevelCleared();
        session.tick();
        LevelCleared cleared = assertInstanceOf(LevelCleared.class, session.phase());
        List<Boolean> flash = new ArrayList<>();
        for (int i = 1; i < LevelCleared.DURATION; i++) {
            session.tick();
            flash.add(cleared.mazeFlashOn());
        }
        // index i-1 holds elapsed i
        for (int elapsed = 1; elapsed <= LevelCleared.STILL; elapsed++) {
            assertFalse(flash.get(elapsed - 1), "still at elapsed " + elapsed);
        }
        assertFalse(flash.get(71 - 1)); // 71 / 12 = 5, odd
        assertTrue(flash.get(72 - 1));
        assertTrue(flash.get(83 - 1));
        assertFalse(flash.get(84 - 1)); // 84 / 12 = 7, odd
    }

    @Test
    void unloadableNextLevelEndsTheMatch() throws InvalidMazeException {
        List<String> maze1 = library.mazeFor(1);
        session = new MatchSession(new MazeLibrary(List.of(maze1, List.of("#"))), 0);
        AtomicInteger gameOvers = new AtomicInteger();
        session.match().events().subscribe(EventType.GAME_OVER, e -> gameOvers.incrementAndGet());

        for (int level = 1; level <= 2; level++) {
            reachPlaying();
            session.match().markLevelCleared();
            session.tick();
            tick(LevelCleared.DURATION);
        }

        assertEquals(PhaseKind.GAME_OVER, session.phase().kind());
        assertTrue(session.isOver());
        assertEquals(1, gameOvers.get());
    }

    @Test
    void caughtAndClearedOnTheSameTickLosesTheLifeFirst() {
        reachPlaying();
        session.match().captureDebugger();
        session.match().markLevelCleared();
        session.tick();
        assertEquals(PhaseKind.LIFE_LOST, session.phase().kind());
    }

    @Test
    void phaseStaysTheSameObjectUntilItChanges() {
        GamePhase ready = session.phase();
        session.tick();
        assertSame(ready, session.phase());
        assertEquals(1, ready.elapsedTicks());
    }

    @Test
    void eachSessionStartsFromAFreshMatch() throws InvalidMazeException {
        reachPlaying();
        session.match().score().add(500);
        session.match().debugger().loseLife();
        tick(30);

        int best = session.match().score().highScore();
        assertTrue(best >= 500);
        MatchSession second = new MatchSession(library, best);
        AtomicInteger leaked = new AtomicInteger();
        second.match().events().subscribe(EventType.BIT_EATEN, e -> leaked.incrementAndGet());
        second.match().events().subscribe(EventType.GAME_OVER, e -> leaked.incrementAndGet());
        session.match().score().add(10);
        tick(600);

        assertNotSame(session.match(), second.match());
        assertEquals(PhaseKind.READY, second.phase().kind());
        assertEquals(0, second.match().score().score());
        assertEquals(best, second.match().score().highScore());
        assertEquals(3, second.match().debugger().lives());
        assertEquals(1, second.match().levelNumber());
        assertEquals(0, leaked.get());
    }
}
