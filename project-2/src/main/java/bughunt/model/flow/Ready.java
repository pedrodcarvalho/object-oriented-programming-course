package bughunt.model.flow;

import bughunt.model.core.Ticks;

/** "READY!": everything stands still for two seconds before each round. */
public final class Ready implements GamePhase {
    public static final int DURATION = Ticks.of(2);

    private int elapsed;

    @Override
    public GamePhase tick(MatchSession session) {
        if (++elapsed < DURATION) {
            return this;
        }
        return new Playing();
    }

    @Override
    public PhaseKind kind() {
        return PhaseKind.READY;
    }

    @Override
    public int elapsedTicks() {
        return elapsed;
    }
}
