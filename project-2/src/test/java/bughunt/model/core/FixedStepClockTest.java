package bughunt.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FixedStepClockTest {
    private FixedStepClock clock;

    @BeforeEach
    void setUp() {
        clock = new FixedStepClock(60, 5);
    }

    @Test
    void oneFrameAtSixtyHertzRunsOneTick() {
        assertEquals(1, clock.advance(1 / 60.0));
    }

    @Test
    void partialFramesAccumulate() {
        assertEquals(0, clock.advance(0.5 / 60));
        assertEquals(1, clock.advance(0.5 / 60));
    }

    @Test
    void longStallRunsAtMostTheCapAndDiscardsTheBacklog() {
        assertEquals(5, clock.advance(2.0));
        assertEquals(0, clock.advance(0));
        assertEquals(1, clock.advance(1 / 60.0));
    }

    @Test
    void manyNormalFramesStayInStep() {
        int ticks = 0;
        for (int i = 0; i < 600; i++) {
            ticks += clock.advance(1 / 60.0);
        }
        assertEquals(600, ticks);
    }

    @Test
    void negativeTimeRunsNothing() {
        assertEquals(0, clock.advance(-1.0));
        assertEquals(1, clock.advance(1 / 60.0));
    }
}
