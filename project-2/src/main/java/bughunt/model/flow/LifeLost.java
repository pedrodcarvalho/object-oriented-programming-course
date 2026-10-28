package bughunt.model.flow;

import bughunt.model.actors.Debugger;
import bughunt.model.core.Ticks;

/**
 * A bug caught the debugger: everything freezes, the cursor spins and then crashes.
 * At the end a life is lost and the round restarts, or the match is over.
 */
public final class LifeLost implements GamePhase {
    /** Until this tick the whole scene is frozen. */
    public static final int FROZEN = Ticks.of(0.7);
    /** Until this tick the cursor spins; after it, it shows the crash. */
    public static final int SPINNING = Ticks.of(1.7);
    public static final int DURATION = Ticks.of(2.4);

    private int elapsed;

    @Override
    public GamePhase tick(MatchSession session) {
        if (++elapsed < DURATION) {
            return this;
        }
        Debugger debugger = session.match().debugger();
        debugger.loseLife();
        if (!debugger.hasLives()) {
            return new GameOver();
        }
        session.match().resetRound();
        return new Ready();
    }

    @Override
    public PhaseKind kind() {
        return PhaseKind.LIFE_LOST;
    }

    @Override
    public int elapsedTicks() {
        return elapsed;
    }
}
