package bughunt.model.flow;

import bughunt.model.core.Match;

/** The match is running. A catch wins over a cleared level when both happen on the same tick. */
public final class Playing implements GamePhase {
    private int elapsed;

    @Override
    public GamePhase tick(MatchSession session) {
        elapsed++;
        Match match = session.match();
        match.tick();
        if (match.debuggerWasCaught()) {
            return new LifeLost();
        }
        if (match.levelWasCleared()) {
            return new LevelCleared();
        }
        return this;
    }

    @Override
    public PhaseKind kind() {
        return PhaseKind.PLAYING;
    }

    @Override
    public int elapsedTicks() {
        return elapsed;
    }
}
