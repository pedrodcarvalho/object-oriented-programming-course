package bughunt.model.flow;

import bughunt.model.core.Ticks;
import bughunt.model.maze.InvalidMazeException;

/**
 * Every pickup collected: the build passed. The maze holds still, then its traces
 * flash until the next level starts. A next level that cannot be loaded ends the match.
 */
public final class LevelCleared implements GamePhase {
    /** Until this tick the maze holds still. */
    public static final int STILL = Ticks.of(1);
    public static final int DURATION = Ticks.of(3);
    /** Length of each flash, on or off. */
    public static final int FLASH = Ticks.of(0.2);

    private int elapsed;

    @Override
    public GamePhase tick(MatchSession session) {
        if (++elapsed < DURATION) {
            return this;
        }
        try {
            session.match().advanceLevel();
            return new Ready();
        } catch (InvalidMazeException e) {
            return new GameOver();
        }
    }

    /** Whether the maze traces are lit in their flash colour right now. */
    public boolean mazeFlashOn() {
        return elapsed > STILL && (elapsed / FLASH) % 2 == 0;
    }

    @Override
    public PhaseKind kind() {
        return PhaseKind.LEVEL_CLEARED;
    }

    @Override
    public int elapsedTicks() {
        return elapsed;
    }
}
