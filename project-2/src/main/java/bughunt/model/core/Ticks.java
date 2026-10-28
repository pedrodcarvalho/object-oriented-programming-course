package bughunt.model.core;

/** The game clock: the model advances at 60 ticks per second. */
public final class Ticks {
    public static final int PER_SECOND = 60;

    private Ticks() {
    }

    /** Converts seconds to the nearest whole number of ticks. */
    public static int of(double seconds) {
        return (int) Math.round(seconds * PER_SECOND);
    }

    /** Converts ticks to whole seconds, rounding up (for countdown displays). */
    public static int toSecondsCeil(int ticks) {
        return (ticks + PER_SECOND - 1) / PER_SECOND;
    }
}
