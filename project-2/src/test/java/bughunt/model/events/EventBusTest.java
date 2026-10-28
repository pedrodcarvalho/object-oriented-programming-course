package bughunt.model.events;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventBusTest {

    @Test
    void listenerReceivesOnlyItsType() {
        EventBus bus = new EventBus();
        List<GameEvent> received = new ArrayList<>();
        bus.subscribe(EventType.BIT_EATEN, received::add);

        GameEvent bit = new GameEvent(EventType.BIT_EATEN, null, 10);
        bus.publish(bit);
        bus.publish(new GameEvent(EventType.GAME_OVER, null, 0));

        assertEquals(List.of(bit), received);
    }

    @Test
    void subscribingDuringPublishIsSafe() {
        EventBus bus = new EventBus();
        bus.subscribe(EventType.ROUND_STARTED, e -> bus.subscribe(EventType.ROUND_STARTED, other -> { }));

        assertDoesNotThrow(() -> bus.publish(new GameEvent(EventType.ROUND_STARTED, null, 0)));
    }
}
