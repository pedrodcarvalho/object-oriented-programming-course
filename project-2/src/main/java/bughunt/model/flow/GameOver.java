package bughunt.model.flow;

import bughunt.model.core.Match;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;

/** No lives left: the match is over. Announces the final score once and then stays put. */
public final class GameOver implements GamePhase {
    private int elapsed;

    @Override
    public void enter(MatchSession session) {
        Match match = session.match();
        match.events().publish(new GameEvent(EventType.GAME_OVER, null, match.score().score()));
    }

    @Override
    public GamePhase tick(MatchSession session) {
        elapsed++;
        return this;
    }

    @Override
    public PhaseKind kind() {
        return PhaseKind.GAME_OVER;
    }

    @Override
    public int elapsedTicks() {
        return elapsed;
    }
}
