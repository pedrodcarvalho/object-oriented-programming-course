package bughunt.model.events;

/** Anyone who wants to be told about a type of event (Observer pattern). */
@FunctionalInterface
public interface EventListener {
    void onEvent(GameEvent event);
}
