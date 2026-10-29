package bughunt.view;

import bughunt.model.core.Direction;
import bughunt.model.core.Position;

/**
 * Where to draw one actor: trailing interpolation between the tile it last left (A) and
 * the tile it is on now (B). The model's progress counts toward a step not yet chosen,
 * so drawing ahead of the actor would cut into walls at every corner; drawing behind it
 * keeps the sprite on two passable tiles at the cost of trailing by up to one tile.
 * Pure state in tile units, no JavaFX, so it can be unit-tested.
 */
final class ActorTrail {
    private Position from;
    private Position to;
    /** How far along A to B, in [0, 1]; never decreases within a segment. */
    private double t;
    private Direction facing;

    /**
     * Records this frame's model state. A new tile starts a new segment from the old one;
     * a tile that is not adjacent (tunnel wrap, back to start) or {@code snap} jumps straight to it.
     */
    void follow(Position position, Direction direction, double progress, boolean snap) {
        if (to == null || snap) {
            from = position;
            to = position;
            t = 0;
        } else if (!position.equals(to)) {
            from = adjacent(to, position) ? to : position;
            to = position;
            t = 0;
        }
        t = Math.max(t, Math.min(1, Math.max(0, progress)));
        facing = from.equals(to) ? direction : towards(from, to);
    }

    /** Column of the actor's tile-space origin (add 0.5 for the centre). */
    double x() {
        return from.x() + (to.x() - from.x()) * t;
    }

    double y() {
        return from.y() + (to.y() - from.y()) * t;
    }

    /** The way the sprite should face: along the segment, or the actor's direction when still. */
    Direction facing() {
        return facing;
    }

    private static boolean adjacent(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) == 1;
    }

    private static Direction towards(Position a, Position b) {
        for (Direction d : Direction.values()) {
            if (a.x() + d.dx() == b.x() && a.y() + d.dy() == b.y()) {
                return d;
            }
        }
        throw new IllegalArgumentException(a + " and " + b + " are not adjacent");
    }
}
