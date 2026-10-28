package bughunt.model.pickups;

import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.core.Ticks;
import bughunt.model.events.EventType;

/** A bonus that appears twice per level, is worth more each level and vanishes after 10 seconds. */
public final class Coffee extends Pickup {
    private static final int DURATION = Ticks.of(10);
    private int ticksLeft = DURATION;

    public Coffee(Position position, int points) {
        super(position, points);
    }

    @Override
    public void update(Match match) {
        if (--ticksLeft <= 0) {
            deactivate();
        }
    }

    @Override
    public void onCollected(Match match) {
        collect(match, EventType.COFFEE_EATEN);
    }

    @Override
    public void onRoundReset(Match match) {
        deactivate();
    }
}
