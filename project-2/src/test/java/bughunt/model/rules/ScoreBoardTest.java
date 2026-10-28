package bughunt.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bughunt.model.core.Position;
import bughunt.model.events.EventBus;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScoreBoardTest {
    private EventBus events;
    private ScoreBoard score;

    @BeforeEach
    void setUp() {
        events = new EventBus();
        score = new ScoreBoard(0);
        score.newMatch(events);
    }

    @Test
    void capturesDoubleUpTo1600() {
        assertEquals(200, score.registerCapture());
        assertEquals(400, score.registerCapture());
        assertEquals(800, score.registerCapture());
        assertEquals(1600, score.registerCapture());
        assertEquals(1600, score.registerCapture());
        assertEquals(4600, score.score());
    }

    @Test
    void breakpointResetsTheCaptureChain() {
        score.registerCapture();
        score.registerCapture();
        events.publish(new GameEvent(EventType.BREAKPOINT_EATEN, new Position(1, 2), 50));
        assertEquals(200, score.registerCapture());
    }

    @Test
    void extraLifeIsGrantedOnceAtTenThousand() {
        AtomicInteger extraLives = new AtomicInteger();
        events.subscribe(EventType.EXTRA_LIFE, e -> extraLives.incrementAndGet());
        score.add(9_990);
        assertEquals(0, extraLives.get());
        score.add(10);
        assertEquals(1, extraLives.get());
        score.add(5_000);
        assertEquals(1, extraLives.get());
    }

    @Test
    void highScoreIsTheBestOfRecordAndScore() {
        ScoreBoard board = new ScoreBoard(5_000);
        board.newMatch(new EventBus());
        board.add(100);
        assertEquals(5_000, board.highScore());
        assertFalse(board.beatHighScore());
        board.add(6_000);
        assertEquals(6_100, board.highScore());
        assertTrue(board.beatHighScore());
    }
}
