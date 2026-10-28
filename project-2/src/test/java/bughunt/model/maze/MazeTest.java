package bughunt.model.maze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bughunt.model.core.Direction;
import bughunt.model.core.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MazeTest {
    private Maze maze;

    @BeforeEach
    void setUp() throws InvalidMazeException {
        maze = new LevelFactory().create(MazeLibrary.fromClasspath().mazeFor(1)).maze();
    }

    @Test
    void tunnelWrapsAround() {
        assertEquals(new Position(27, 10), maze.neighbor(new Position(0, 10), Direction.LEFT));
    }

    @Test
    void doorNeedsBacklogAccess() {
        Position door = new Position(13, 9);
        assertFalse(maze.isPassable(door, false));
        assertTrue(maze.isPassable(door, true));
    }

    @Test
    void shortestPathGoesThroughTheDoor() {
        assertEquals(Direction.DOWN, maze.shortestPathDirection(new Position(13, 8), new Position(13, 10), true));
    }
}
