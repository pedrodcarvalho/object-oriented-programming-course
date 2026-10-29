package bughunt.view;

import bughunt.model.core.Position;
import bughunt.model.maze.CellType;
import bughunt.model.maze.Maze;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * The static circuit board of one maze: the dark PCB with its faint grid, the wall
 * outline as glowing green traces and the backlog door as a magenta line. Built once
 * per maze; the only thing that changes afterwards is the trace colour (level-clear flash).
 */
final class MazeRenderer {
    private static final double TRACE_WIDTH = 3;
    private static final double DOOR_WIDTH = 3;
    private static final double GLOW_RADIUS = 12;
    private static final double GLOW_SPREAD = 0.35;

    private final Group root = new Group();
    private final Path traces = new Path();
    private final DropShadow glow = new DropShadow(BlurType.GAUSSIAN, Theme.TRACE, GLOW_RADIUS, GLOW_SPREAD, 0, 0);
    private boolean flashing;

    MazeRenderer(Maze maze) {
        double width = maze.width() * Theme.TILE;
        double height = maze.height() * Theme.TILE;

        Rectangle board = new Rectangle(width, height, Theme.BACKGROUND);
        Path grid = gridLines(maze, width, height);

        for (WallOutline.Segment s : WallOutline.compute(maze)) {
            traces.getElements().addAll(
                    new MoveTo(s.x1() * Theme.TILE, s.y1() * Theme.TILE),
                    new LineTo(s.x2() * Theme.TILE, s.y2() * Theme.TILE));
        }
        styleLine(traces, Theme.TRACE, TRACE_WIDTH);

        // One glow for the whole trace layer (not one per node), and the layer is cached
        // as a bitmap so actors moving over it do not force the blur to be recomputed.
        // The default hint re-renders on a window rescale, keeping the traces sharp.
        Group traceLayer = new Group(traces, doorLine(maze));
        traceLayer.setEffect(glow);
        traceLayer.setCache(true);
        traceLayer.setCacheHint(CacheHint.DEFAULT);

        root.getChildren().addAll(board, grid, traceLayer);
    }

    Node node() {
        return root;
    }

    /** Lights the traces white (level-clear flash) or back to green. */
    void setFlash(boolean on) {
        if (on == flashing) {
            return;
        }
        flashing = on;
        Color colour = on ? Theme.FLASH : Theme.TRACE;
        traces.setStroke(colour);
        glow.setColor(colour);
    }

    private static Path gridLines(Maze maze, double width, double height) {
        Path grid = new Path();
        for (int x = 1; x < maze.width(); x++) {
            grid.getElements().addAll(new MoveTo(x * Theme.TILE, 0), new LineTo(x * Theme.TILE, height));
        }
        for (int y = 1; y < maze.height(); y++) {
            grid.getElements().addAll(new MoveTo(0, y * Theme.TILE), new LineTo(width, y * Theme.TILE));
        }
        grid.setStroke(Theme.GRID);
        grid.setStrokeWidth(1);
        return grid;
    }

    /**
     * A line through the middle of every door cell, across the passage: horizontal when
     * the door sits in a horizontal wall (a wall or door to its left or right), else vertical.
     */
    private static Path doorLine(Maze maze) {
        Path door = new Path();
        for (int y = 0; y < maze.height(); y++) {
            for (int x = 0; x < maze.width(); x++) {
                if (type(maze, x, y) != CellType.DOOR) {
                    continue;
                }
                double left = x * Theme.TILE;
                double top = y * Theme.TILE;
                double half = Theme.TILE / 2.0;
                if (blocks(type(maze, x - 1, y)) || blocks(type(maze, x + 1, y))) {
                    door.getElements().addAll(new MoveTo(left, top + half), new LineTo(left + Theme.TILE, top + half));
                } else {
                    door.getElements().addAll(new MoveTo(left + half, top), new LineTo(left + half, top + Theme.TILE));
                }
            }
        }
        styleLine(door, Theme.DOOR, DOOR_WIDTH);
        return door;
    }

    private static boolean blocks(CellType type) {
        return type == CellType.WALL || type == CellType.DOOR;
    }

    private static CellType type(Maze maze, int x, int y) {
        return maze.cell(new Position(x, y)).type();
    }

    private static void styleLine(Path path, Color colour, double width) {
        path.setStroke(colour);
        path.setStrokeWidth(width);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setFill(null);
    }
}
