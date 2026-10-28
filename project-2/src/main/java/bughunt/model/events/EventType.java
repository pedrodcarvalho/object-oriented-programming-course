package bughunt.model.events;

/** Everything that can happen during a match and interest other classes. */
public enum EventType {
    BIT_EATEN,
    BREAKPOINT_EATEN,
    COFFEE_EATEN,
    COFFEE_SPAWNED,
    BUG_CAPTURED,
    BUG_RELEASED,
    DEBUGGER_CAUGHT,
    EXTRA_LIFE,
    ROUND_STARTED,
    LEVEL_CLEARED,
    GAME_OVER
}
