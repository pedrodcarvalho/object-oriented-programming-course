package bughunt.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bughunt.model.core.Direction;
import bughunt.model.core.Position;
import org.junit.jupiter.api.Test;

class ActorTrailTest {
    private static final double EPS = 1e-9;
    private final ActorTrail trail = new ActorTrail();

    @Test
    void firstSightingIsDrawnOnTheTile() {
        trail.follow(new Position(3, 4), Direction.LEFT, 0.6, false);

        assertAt(3, 4);
        assertEquals(Direction.LEFT, trail.facing());
    }

    @Test
    void movesFromThePreviousTileTowardTheCurrentOneByProgress() {
        trail.follow(new Position(3, 4), Direction.RIGHT, 0.9, false);
        trail.follow(new Position(4, 4), Direction.RIGHT, 0.25, false);
        assertAt(3.25, 4);

        trail.follow(new Position(4, 4), Direction.RIGHT, 0.75, false);
        assertAt(3.75, 4);
    }

    @Test
    void neverMovesBackwardWhenTheActorIsBlocked() {
        trail.follow(new Position(3, 4), Direction.RIGHT, 0, false);
        trail.follow(new Position(4, 4), Direction.RIGHT, 0.9, false);
        // Blocked against a wall: the model resets progress to 0.
        trail.follow(new Position(4, 4), Direction.RIGHT, 0, false);

        assertAt(3.9, 4);
    }

    @Test
    void facesAlongTheSegmentNotTheArrivalDirection() {
        trail.follow(new Position(3, 4), Direction.RIGHT, 0, false);
        // Arrived at (3, 3) moving UP; direction() may already report the next turn.
        trail.follow(new Position(3, 3), Direction.LEFT, 0.1, false);

        assertEquals(Direction.UP, trail.facing());
    }

    @Test
    void snapsAcrossATunnelWrap() {
        trail.follow(new Position(0, 10), Direction.LEFT, 0.5, false);
        trail.follow(new Position(27, 10), Direction.LEFT, 0.1, false);

        assertAt(27, 10);
        assertEquals(Direction.LEFT, trail.facing());
    }

    @Test
    void snapsWhenAskedEvenToAnAdjacentTile() {
        trail.follow(new Position(3, 4), Direction.RIGHT, 0, false);
        trail.follow(new Position(4, 4), Direction.LEFT, 0, true);

        assertAt(4, 4);
        assertEquals(Direction.LEFT, trail.facing());
    }

    @Test
    void clampsProgressToOne() {
        trail.follow(new Position(3, 4), Direction.DOWN, 0, false);
        trail.follow(new Position(3, 5), Direction.DOWN, 1.5, false);

        assertAt(3, 5);
    }

    private void assertAt(double x, double y) {
        assertEquals(x, trail.x(), EPS, "x");
        assertEquals(y, trail.y(), EPS, "y");
    }
}
