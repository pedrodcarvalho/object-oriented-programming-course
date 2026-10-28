package bughunt.model.maze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bughunt.model.core.Position;
import java.util.List;
import org.junit.jupiter.api.Test;

class LevelFactoryTest {
    private final LevelFactory factory = new LevelFactory();

    private static List<String> maze1() throws InvalidMazeException {
        return MazeLibrary.fromClasspath().mazeFor(1);
    }

    @Test
    void buildsMaze1() throws InvalidMazeException {
        Level level = factory.create(maze1());
        Maze maze = level.maze();
        assertEquals(28, maze.width());
        assertEquals(23, maze.height());
        assertEquals(new Position(13, 17), level.debuggerStart());
        assertEquals(4, level.bugs().size());
        assertEquals(210, level.pickups().size());
        assertEquals(new Position(13, 8), maze.backlogExit());
        assertEquals(new Position(13, 10), maze.backlogCenter());
        assertEquals(new Position(13, 13), maze.coffeeSpot());
    }

    @Test
    void rejectsLinesOfUnequalLength() {
        InvalidMazeException e = assertThrows(InvalidMazeException.class,
                () -> factory.create(List.of("####", "###")));
        assertTrue(e.getMessage().contains("Line 2"), e.getMessage());
    }

    @Test
    void rejectsUnknownCharacters() {
        InvalidMazeException e = assertThrows(InvalidMazeException.class,
                () -> factory.create(List.of("#X##")));
        assertTrue(e.getMessage().contains("column"), e.getMessage());
    }

    @Test
    void rejectsMazeWithoutDebuggerStart() throws InvalidMazeException {
        List<String> withoutP = maze1().stream().map(line -> line.replace('P', ' ')).toList();
        assertThrows(InvalidMazeException.class, () -> factory.create(withoutP));
    }
}
