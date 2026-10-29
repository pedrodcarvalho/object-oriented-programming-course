package bughunt.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bughunt.model.core.Position;
import bughunt.model.maze.Cell;
import bughunt.model.maze.CellType;
import bughunt.model.maze.InvalidMazeException;
import bughunt.model.maze.LevelFactory;
import bughunt.model.maze.Maze;
import bughunt.model.maze.MazeLibrary;
import bughunt.view.WallOutline.Segment;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WallOutlineTest {

    @Test
    void singleWallInTheMiddleIsOutlinedOnAllFourSides() {
        Maze maze = maze(
                "   ",
                " # ",
                "   ");

        assertSegments(WallOutline.compute(maze),
                new Segment(1, 1, 2, 1),
                new Segment(1, 2, 2, 2),
                new Segment(1, 1, 1, 2),
                new Segment(2, 1, 2, 2));
    }

    @Test
    void collinearEdgesAreMergedIntoOneSegment() {
        Maze maze = maze(
                "     ",
                " ### ",
                "     ");

        assertSegments(WallOutline.compute(maze),
                new Segment(1, 1, 4, 1),
                new Segment(1, 2, 4, 2),
                new Segment(1, 1, 1, 2),
                new Segment(4, 1, 4, 2));
    }

    @Test
    void edgesOnTheMapBoundaryAreNotOutlined() {
        Maze maze = maze(
                "##",
                "  ");

        assertSegments(WallOutline.compute(maze), new Segment(0, 1, 2, 1));
    }

    @Test
    void maze1OutlineStaysOnTheGrid() throws InvalidMazeException {
        Maze maze = new LevelFactory().create(MazeLibrary.fromClasspath().mazeFor(1)).maze();

        List<Segment> segments = WallOutline.compute(maze);

        assertFalse(segments.isEmpty());
        for (Segment s : segments) {
            for (double x : new double[] {s.x1(), s.x2()}) {
                assertTrue(x == Math.rint(x) && x >= 0 && x <= 28, "x out of the grid: " + s);
            }
            for (double y : new double[] {s.y1(), s.y2()}) {
                assertTrue(y == Math.rint(y) && y >= 0 && y <= 23, "y out of the grid: " + s);
            }
        }
    }

    /** Builds a maze from rows of '#' (wall) and ' ' (corridor). */
    private static Maze maze(String... rows) {
        Cell[][] cells = new Cell[rows.length][rows[0].length()];
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                cells[y][x] = new Cell(rows[y].charAt(x) == '#' ? CellType.WALL : CellType.CORRIDOR);
            }
        }
        Position origin = new Position(0, 0);
        return new Maze(cells, origin, origin, origin);
    }

    /** Compares as sets, ignoring segment order and endpoint order. */
    private static void assertSegments(List<Segment> actual, Segment... expected) {
        assertEquals(normalise(List.of(expected)), normalise(actual));
        assertEquals(expected.length, actual.size(), "duplicate segments: " + actual);
    }

    private static Set<Segment> normalise(List<Segment> segments) {
        Set<Segment> result = new HashSet<>();
        for (Segment s : segments) {
            boolean ordered = s.x1() < s.x2() || (s.x1() == s.x2() && s.y1() <= s.y2());
            result.add(ordered ? s : new Segment(s.x2(), s.y2(), s.x1(), s.y1()));
        }
        return result;
    }
}
