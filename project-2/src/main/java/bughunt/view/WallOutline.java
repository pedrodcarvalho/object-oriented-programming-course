package bughunt.view;

import bughunt.model.core.Position;
import bughunt.model.maze.CellType;
import bughunt.model.maze.Maze;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns the wall cells of a maze into the line segments of their outline, so the
 * renderer can draw walls as glowing circuit traces instead of filled blocks.
 * Pure geometry in tile units: no JavaFX here, so it can be unit-tested.
 */
public final class WallOutline {

    /** A straight line from (x1, y1) to (x2, y2), in tile units. */
    public record Segment(double x1, double y1, double x2, double y2) {
    }

    private WallOutline() {
    }

    /**
     * Every edge between a WALL cell and a non-WALL cell, with collinear neighbouring
     * edges merged. Edges on the map boundary have no neighbour cell and are skipped.
     */
    public static List<Segment> compute(Maze maze) {
        int width = maze.width();
        int height = maze.height();
        List<Segment> segments = new ArrayList<>();

        // Horizontal grid lines strictly inside the map: the edge at column x on line y
        // separates cell (x, y - 1) from cell (x, y).
        for (int y = 1; y < height; y++) {
            int runStart = -1;
            for (int x = 0; x <= width; x++) {
                boolean edge = x < width && isWall(maze, x, y - 1) != isWall(maze, x, y);
                if (edge && runStart < 0) {
                    runStart = x;
                } else if (!edge && runStart >= 0) {
                    segments.add(new Segment(runStart, y, x, y));
                    runStart = -1;
                }
            }
        }

        // Vertical grid lines strictly inside the map: the edge at row y on line x
        // separates cell (x - 1, y) from cell (x, y).
        for (int x = 1; x < width; x++) {
            int runStart = -1;
            for (int y = 0; y <= height; y++) {
                boolean edge = y < height && isWall(maze, x - 1, y) != isWall(maze, x, y);
                if (edge && runStart < 0) {
                    runStart = y;
                } else if (!edge && runStart >= 0) {
                    segments.add(new Segment(x, runStart, x, y));
                    runStart = -1;
                }
            }
        }
        return segments;
    }

    private static boolean isWall(Maze maze, int x, int y) {
        return maze.cell(new Position(x, y)).type() == CellType.WALL;
    }
}
