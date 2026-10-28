package bughunt.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LevelConfigTest {
    private static final double DELTA = 1e-9;

    @Test
    void speedsAtLevelOne() {
        LevelConfig level1 = LevelConfig.forLevel(1);
        assertEquals(6.8, level1.debuggerSpeed(), DELTA);
        assertEquals(6.375, level1.bugSpeed(), DELTA);
        assertEquals(13.6, level1.capturedBugSpeed(), DELTA);
    }

    @Test
    void breakpointShrinksButNeverBelowOneSecond() {
        assertEquals(240, LevelConfig.forLevel(3).breakpointDuration());
        assertEquals(60, LevelConfig.forLevel(20).breakpointDuration());
    }

    @Test
    void flashingIsCappedByHalfTheBreakpoint() {
        assertEquals(120, LevelConfig.forLevel(1).flashingTicks());
        assertEquals(30, LevelConfig.forLevel(6).flashingTicks());
    }

    @Test
    void coffeeValueGrowsAndCapsAt5000() {
        assertEquals(100, LevelConfig.forLevel(1).coffeeValue());
        assertEquals(5000, LevelConfig.forLevel(20).coffeeValue());
    }

    @Test
    void wavesEndWithEndlessChase() {
        int[] waves = LevelConfig.forLevel(1).wavesInTicks();
        assertEquals(8, waves.length);
        assertEquals(420, waves[0]);
        assertEquals(1200, waves[1]);
        assertEquals(Integer.MAX_VALUE, waves[waves.length - 1]);
        assertEquals(300, LevelConfig.forLevel(5).wavesInTicks()[0]);
    }

    @Test
    void releaseDelays() {
        assertEquals(0, LevelConfig.forLevel(1).releaseDelay(0));
        assertEquals(300, LevelConfig.forLevel(1).releaseDelay(2));
        assertEquals(300, LevelConfig.forLevel(5).releaseDelay(3));
    }

    @Test
    void levelIsClampedToAtLeastOne() {
        assertEquals(1, LevelConfig.forLevel(0).number());
    }
}
