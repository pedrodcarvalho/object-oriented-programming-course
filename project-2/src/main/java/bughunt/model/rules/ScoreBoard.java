package bughunt.model.rules;

import bughunt.model.events.EventBus;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;

/**
 * Points, the high score and the extra life at 10,000 points.
 * Captures with the same breakpoint are worth 200, 400, 800 and 1,600 points.
 * The best score is handed in from outside; saving it is someone else's job.
 */
public class ScoreBoard {
    public static final int EXTRA_LIFE_AT = 10_000;
    private static final int FIRST_CAPTURE = 200;

    private EventBus events;
    private int score;
    private int highScore;
    private int highScoreAtStart;
    private int capturesInARow;
    private boolean extraLifeGranted;

    public ScoreBoard(int bestScore) {
        this.highScore = bestScore;
    }

    /** Resets the points and starts listening to the new match's events. */
    public void newMatch(EventBus matchEvents) {
        this.events = matchEvents;
        score = 0;
        capturesInARow = 0;
        extraLifeGranted = false;
        highScoreAtStart = highScore;
        events.subscribe(EventType.BIT_EATEN, e -> add(e.value()));
        events.subscribe(EventType.COFFEE_EATEN, e -> add(e.value()));
        events.subscribe(EventType.BREAKPOINT_EATEN, e -> {
            capturesInARow = 0;
            add(e.value());
        });
    }

    /** The only way points change. */
    public void add(int value) {
        score += value;
        if (score > highScore) {
            highScore = score;
        }
        if (!extraLifeGranted && score >= EXTRA_LIFE_AT) {
            extraLifeGranted = true;
            events.publish(new GameEvent(EventType.EXTRA_LIFE, null, 1));
        }
    }

    /** Adds and returns the value of one more capture with the current breakpoint. */
    public int registerCapture() {
        int value = FIRST_CAPTURE << Math.min(capturesInARow, 3);
        capturesInARow++;
        add(value);
        return value;
    }

    public int score() {
        return score;
    }

    public int highScore() {
        return highScore;
    }

    public boolean beatHighScore() {
        return score > 0 && score > highScoreAtStart;
    }
}
