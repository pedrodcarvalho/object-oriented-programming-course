package bughunt.model.flow;

/** The phases a match goes through, so the view can tell them apart without type checks. */
public enum PhaseKind {
    READY,
    PLAYING,
    LIFE_LOST,
    LEVEL_CLEARED,
    GAME_OVER
}
