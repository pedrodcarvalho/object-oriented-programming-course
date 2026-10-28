package bughunt.model.pickups;

import bughunt.model.core.Entity;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;

/** An item sitting on a tile that the Debugger can pick up. Each kind reacts its own way. */
public abstract sealed class Pickup extends Entity permits Bit, Breakpoint, Coffee {
    private final int points;

    protected Pickup(Position position, int points) {
        super(position);
        this.points = points;
    }

    public int points() {
        return points;
    }

    /** The effect of being picked up by the Debugger. */
    public abstract void onCollected(Match match);

    /** Leaves the maze and tells the match (the ScoreBoard adds the points when it hears the event). */
    protected void collect(Match match, EventType type) {
        deactivate();
        match.events().publish(new GameEvent(type, position, points));
    }

    @Override
    public void update(Match match) {
        // most pickups just sit there
    }
}
