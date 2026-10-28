package bughunt.model.core;

/**
 * Turns variable frame times into a whole number of fixed model ticks.
 * After a long stall (window drag, GC pause) it runs at most a few ticks and
 * drops the rest, so the game never races to catch up.
 */
public final class FixedStepClock {
    /** Absorbs floating-point error so that a frame of exactly 1/60 s yields one tick. */
    private static final double EPSILON = 1e-9;

    private final int ticksPerSecond;
    private final int maxTicksPerFrame;
    private double pendingTicks;

    public FixedStepClock(int ticksPerSecond, int maxTicksPerFrame) {
        if (ticksPerSecond <= 0 || maxTicksPerFrame <= 0) {
            throw new IllegalArgumentException("Tick rate and per-frame cap must be positive.");
        }
        this.ticksPerSecond = ticksPerSecond;
        this.maxTicksPerFrame = maxTicksPerFrame;
    }

    /** Adds the frame's elapsed time and returns how many ticks to run now. */
    public int advance(double seconds) {
        if (seconds > 0) {
            pendingTicks += seconds * ticksPerSecond;
        }
        int ticks = (int) Math.floor(pendingTicks + EPSILON);
        if (ticks > maxTicksPerFrame) {
            pendingTicks = 0;
            return maxTicksPerFrame;
        }
        pendingTicks = Math.max(0, pendingTicks - ticks);
        return ticks;
    }
}
