package bughunt.model.maze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MazeLibraryTest {

    @Test
    void mazesRotateEveryTwoLevels() {
        assertEquals(0, MazeLibrary.indexFor(1, 3));
        assertEquals(0, MazeLibrary.indexFor(2, 3));
        assertEquals(1, MazeLibrary.indexFor(3, 3));
        assertEquals(2, MazeLibrary.indexFor(5, 3));
        assertEquals(0, MazeLibrary.indexFor(7, 3));
        assertEquals(1, MazeLibrary.indexFor(40, 3));
    }

    @Test
    void classpathLibraryHasEveryListedMaze() throws InvalidMazeException {
        assertEquals(MazeLibrary.MAZE_FILES.size(), MazeLibrary.fromClasspath().mazeCount());
    }

    @Test
    void maze1IsReadWithoutTrailingEmptyLines() throws InvalidMazeException {
        List<String> maze1 = MazeLibrary.fromClasspath().mazeFor(1);
        assertEquals(23, maze1.size());
        assertEquals("############################", maze1.get(22));
    }

    @Test
    void missingFileThrowsNamingTheFile() {
        InvalidMazeException e = assertThrows(InvalidMazeException.class,
                () -> MazeLibrary.fromClasspath(List.of("no-such.maze")));
        assertTrue(e.getMessage().contains("no-such.maze"), e.getMessage());
    }
}
