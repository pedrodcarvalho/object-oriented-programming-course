package bughunt.model.events;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Notifies interested parties when something happens, without the classes knowing each other.
 * E.g. a bit only publishes BIT_EATEN; the scoreboard and the level manager each react their own way.
 */
public class EventBus {
    private final Map<EventType, List<EventListener>> listeners = new EnumMap<>(EventType.class);

    public void subscribe(EventType type, EventListener listener) {
        listeners.computeIfAbsent(type, t -> new ArrayList<>()).add(listener);
    }

    /** Iterates over a copy, so listeners may subscribe others while being notified. */
    public void publish(GameEvent event) {
        for (EventListener listener : List.copyOf(listeners.getOrDefault(event.type(), List.of()))) {
            listener.onEvent(event);
        }
    }
}
