package bughunt.model.flow;

import bughunt.model.core.Direction;
import bughunt.model.core.Match;
import bughunt.model.maze.InvalidMazeException;
import bughunt.model.maze.MazeLibrary;
import bughunt.model.rules.ScoreBoard;

/**
 * One play-through, from the first "READY!" to game over: a brand-new match and the
 * phase it is in. The app ticks it and forwards input; the view reads it.
 */
public final class MatchSession {
    private final Match match;
    private GamePhase phase;

    public MatchSession(MazeLibrary library, int bestScore) throws InvalidMazeException {
        this.match = new Match(library, new ScoreBoard(bestScore));
        changeTo(new Ready());
    }

    /** One fixed-step tick of the current phase. */
    public void tick() {
        GamePhase next = phase.tick(this);
        if (next != phase) {
            changeTo(next);
        }
    }

    /** Steering only counts while a round is about to start or running. */
    public void requestDirection(Direction direction) {
        PhaseKind kind = phase.kind();
        if (kind == PhaseKind.READY || kind == PhaseKind.PLAYING) {
            match.debugger().requestDirection(direction);
        }
    }

    public GamePhase phase() {
        return phase;
    }

    public Match match() {
        return match;
    }

    public boolean isOver() {
        return phase.kind() == PhaseKind.GAME_OVER;
    }

    private void changeTo(GamePhase next) {
        phase = next;
        next.enter(this);
    }
}
