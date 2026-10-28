package bughunt.model.core;

/**
 * A maze tile: x is the column and y is the row.
 * It is immutable: moving returns a new position instead of changing this one.
 */
public record Position(int x, int y) {

    public Position move(Direction direction) {
        return move(direction, 1);
    }

    public Position move(Direction direction, int tiles) {
        return new Position(x + direction.dx() * tiles, y + direction.dy() * tiles);
    }

    public double distanceTo(Position other) {
        return Math.hypot(x - other.x, y - other.y);
    }
}
