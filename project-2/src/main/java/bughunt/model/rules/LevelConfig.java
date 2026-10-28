package bughunt.model.rules;

import bughunt.model.core.Ticks;

/**
 * Numbers that change from level to level: speeds, breakpoint duration,
 * coffee value, scatter/chase waves and backlog waiting time.
 * Based on the original Pac-Man tables, with speeds in tiles per second.
 */
public final class LevelConfig {
    private static final double MAX_SPEED = 8.5;
    private static final int[] COFFEE_VALUES = {100, 300, 500, 500, 700, 700, 1000, 1000, 2000, 2000, 3000, 3000, 5000};

    private final int number;

    private LevelConfig(int number) {
        this.number = number;
    }

    public static LevelConfig forLevel(int level) {
        return new LevelConfig(Math.max(1, level));
    }

    public int number() {
        return number;
    }

    private double speed(double level1, double levels2to4, double later) {
        double factor = number == 1 ? level1 : number <= 4 ? levels2to4 : later;
        return MAX_SPEED * factor;
    }

    public double debuggerSpeed() {
        return speed(0.80, 0.90, 1.00);
    }

    public double bugSpeed() {
        return speed(0.75, 0.85, 0.95);
    }

    public double vulnerableBugSpeed() {
        return speed(0.50, 0.55, 0.60);
    }

    public double tunnelBugSpeed() {
        return speed(0.40, 0.45, 0.50);
    }

    public double backlogSpeed() {
        return speed(0.40, 0.45, 0.50);
    }

    public double capturedBugSpeed() {
        return MAX_SPEED * 1.6;
    }

    /** 6 s at level 1, one second less per level, never less than 1 s. */
    public int breakpointDuration() {
        return Ticks.of(Math.max(1, 7 - number));
    }

    /** During the last moments of the breakpoint the bugs flash white. */
    public int flashingTicks() {
        return Math.min(Ticks.of(2), breakpointDuration() / 2);
    }

    public int coffeeValue() {
        return COFFEE_VALUES[Math.min(number, COFFEE_VALUES.length) - 1];
    }

    /** Alternating durations: scatter, chase, scatter... The last wave (chase) never ends. */
    public int[] wavesInTicks() {
        double[] seconds;
        if (number == 1) {
            seconds = new double[]{7, 20, 7, 20, 5, 20, 5};
        } else if (number <= 4) {
            seconds = new double[]{7, 20, 7, 20, 5};
        } else {
            seconds = new double[]{5, 20, 5, 20, 5};
        }
        int[] waves = new int[seconds.length + 1];
        for (int i = 0; i < seconds.length; i++) {
            waves[i] = Ticks.of(seconds[i]);
        }
        waves[seconds.length] = Integer.MAX_VALUE;
        return waves;
    }

    /** How long each bug waits in the backlog at round start (order 0 starts outside). */
    public int releaseDelay(int order) {
        if (order == 0) {
            return 0;
        }
        double interval = number == 1 ? 4 : number <= 4 ? 3 : 2;
        return Ticks.of(1 + (order - 1) * interval);
    }
}
