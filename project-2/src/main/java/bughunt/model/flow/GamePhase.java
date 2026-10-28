package bughunt.model.flow;

/**
 * One phase of a match (State pattern): each tick it either stays ({@code this})
 * or hands over to the next phase. The session calls {@link #enter} on every change.
 */
public interface GamePhase {

    /** Called once when this phase becomes the current one. */
    default void enter(MatchSession session) {
    }

    /** Advances one tick and returns the phase to use from now on. */
    GamePhase tick(MatchSession session);

    PhaseKind kind();

    /** Ticks spent in this phase so far (the view times its animations with it). */
    int elapsedTicks();
}
