package bughunt.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bughunt.model.TestMatches;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;
import bughunt.model.pickups.Coffee;
import bughunt.model.pickups.Pickup;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LevelManagerTest {
    private static final Position COFFEE_SPOT = new Position(13, 13);

    private Match match;
    private final List<GameEvent> published = new ArrayList<>();

    @BeforeEach
    void setUp() {
        match = TestMatches.maze1Match();
        for (EventType type : List.of(EventType.COFFEE_SPAWNED, EventType.LEVEL_CLEARED)) {
            match.events().subscribe(type, published::add);
        }
    }

    private long count(EventType type) {
        return published.stream().filter(e -> e.type() == type).count();
    }

    private boolean hasCoffee() {
        return match.pickups().stream().anyMatch(p -> p instanceof Coffee && p.position().equals(COFFEE_SPOT));
    }

    @Test
    void coffeeAppearsAfterSeventyPickups() {
        for (int i = 0; i < 69; i++) {
            match.events().publish(new GameEvent(EventType.BIT_EATEN, new Position(1, 1), 10));
        }
        assertFalse(hasCoffee());
        match.events().publish(new GameEvent(EventType.BIT_EATEN, new Position(1, 1), 10));
        assertTrue(hasCoffee());
        assertEquals(1, count(EventType.COFFEE_SPAWNED));
        assertEquals(COFFEE_SPOT, published.get(0).position());
    }

    @Test
    void collectingEverythingClearsTheLevelOnce() {
        List<Pickup> all = List.copyOf(match.pickups());
        assertEquals(210, all.size());
        for (Pickup pickup : all) {
            pickup.onCollected(match);
        }
        assertTrue(match.levelWasCleared());
        assertEquals(0, match.pickupsRemaining());
        assertEquals(1, count(EventType.LEVEL_CLEARED));
        assertEquals(2, count(EventType.COFFEE_SPAWNED));
    }
}
