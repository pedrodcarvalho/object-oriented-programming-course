package bughunt.model.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void moveSeveralTilesInADirection() {
        assertEquals(new Position(-1, 4), new Position(3, 4).move(Direction.LEFT, 4));
    }

    @Test
    void oppositeOfUpIsDown() {
        assertEquals(Direction.DOWN, Direction.UP.opposite());
    }

    @Test
    void distanceIsEuclidean() {
        assertEquals(5.0, new Position(0, 0).distanceTo(new Position(3, 4)), 1e-9);
    }

    @Test
    void declarationOrderIsTheTieBreakOrder() {
        assertArrayEquals(
                new Direction[]{Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT},
                Direction.values());
    }
}
