package bughunt.model.maze;

import bughunt.model.core.Direction;
import bughunt.model.core.Position;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * The circuit board: a grid of cells read from the level file.
 * The edges wrap around (the side tunnel leads to the opposite side).
 */
public class Maze {
    private final Cell[][] cells;
    private final int width;
    private final int height;
    private final Position backlogExit;
    private final Position backlogCenter;
    private final Position coffeeSpot;

    /** {@code cells[y][x]}: rows first, then columns. */
    public Maze(Cell[][] cells, Position backlogExit, Position backlogCenter, Position coffeeSpot) {
        this.cells = cells;
        this.height = cells.length;
        this.width = cells[0].length;
        this.backlogExit = backlogExit;
        this.backlogCenter = backlogCenter;
        this.coffeeSpot = coffeeSpot;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** The tile just above the door: where bugs leave and enter the backlog. */
    public Position backlogExit() {
        return backlogExit;
    }

    /** The tile just below the door, inside the backlog. */
    public Position backlogCenter() {
        return backlogCenter;
    }

    public Position coffeeSpot() {
        return coffeeSpot;
    }

    public Cell cell(Position p) {
        return cells[Math.floorMod(p.y(), height)][Math.floorMod(p.x(), width)];
    }

    /** The neighbouring tile in the given direction, wrapping around the edges (tunnel). */
    public Position neighbor(Position p, Direction direction) {
        Position n = p.move(direction);
        return new Position(Math.floorMod(n.x(), width), Math.floorMod(n.y(), height));
    }

    public boolean isPassable(Position p, boolean backlogAccess) {
        return cell(p).type().isPassable(backlogAccess);
    }

    public boolean isTunnel(Position p) {
        return cell(p).type() == CellType.TUNNEL;
    }

    /**
     * First step of the shortest path between two tiles (breadth-first search), or null
     * if there is none or both are the same tile. Used by captured bugs returning to the
     * backlog and by bugs leaving it.
     */
    public Direction shortestPathDirection(Position from, Position to, boolean backlogAccess) {
        if (from.equals(to)) {
            return null;
        }
        Direction[][] firstStep = new Direction[height][width];
        boolean[][] visited = new boolean[height][width];
        Queue<Position> queue = new ArrayDeque<>();
        visited[from.y()][from.x()] = true;
        for (Direction d : Direction.values()) {
            Position n = neighbor(from, d);
            if (isPassable(n, backlogAccess) && !visited[n.y()][n.x()]) {
                visited[n.y()][n.x()] = true;
                firstStep[n.y()][n.x()] = d;
                queue.add(n);
            }
        }
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(to)) {
                return firstStep[current.y()][current.x()];
            }
            for (Direction d : Direction.values()) {
                Position n = neighbor(current, d);
                if (isPassable(n, backlogAccess) && !visited[n.y()][n.x()]) {
                    visited[n.y()][n.x()] = true;
                    firstStep[n.y()][n.x()] = firstStep[current.y()][current.x()];
                    queue.add(n);
                }
            }
        }
        return null;
    }
}
