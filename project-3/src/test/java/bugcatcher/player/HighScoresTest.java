package bugcatcher.player;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HighScoresTest {

    @TempDir
    Path folder;

    @Test
    void startsEmptyWhenThereIsNoFile() {
        HighScores scores = new HighScores(folder.resolve("none.txt"));
        assertArrayEquals(new int[] {0, 0, 0, 0, 0}, scores.getScores());
    }

    @Test
    void keepsOnlyTheFiveBestBiggestFirst() {
        HighScores scores = new HighScores(folder.resolve("s.txt"), 30, 10, 70, 50, 20, 60);
        assertArrayEquals(new int[] {70, 60, 50, 30, 20}, scores.getScores());
        assertEquals(70, scores.getBest());
        assertFalse(scores.add(5));
        assertTrue(scores.contains(50));
        assertFalse(scores.contains(10));
    }

    @Test
    void savesAndLoadsAgain() {
        Path file = folder.resolve("s.txt");
        new HighScores(file, 100, 40).save();

        HighScores loaded = new HighScores(file);
        assertArrayEquals(new int[] {100, 40, 0, 0, 0}, loaded.getScores());
    }

    @Test
    void ignoresBadLinesInTheFile() throws IOException {
        Path file = folder.resolve("s.txt");
        Files.write(file, List.of("80", "hello", "-3", " 25 "));

        HighScores loaded = new HighScores(file);
        assertArrayEquals(new int[] {80, 25, 0, 0, 0}, loaded.getScores());
    }

    @Test
    void getScoresReturnsACopy() {
        HighScores scores = new HighScores(folder.resolve("s.txt"), 10);
        scores.getScores()[0] = 999;
        assertEquals(10, scores.getBest());
    }
}
