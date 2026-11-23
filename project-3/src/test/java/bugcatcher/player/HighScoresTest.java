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
        assertEquals("---", scores.getEntries()[0].getName());
    }

    @Test
    void keepsOnlyTheFiveBestBiggestFirst() {
        HighScores scores = new HighScores(folder.resolve("s.txt"),
                new ScoreEntry("Ana", 30), new ScoreEntry("Bia", 10), new ScoreEntry("Caio", 70),
                new ScoreEntry("Davi", 50), new ScoreEntry("Eva", 20), new ScoreEntry("Fabio", 60));
        assertArrayEquals(new int[] {70, 60, 50, 30, 20}, scores.getScores());
        assertEquals(70, scores.getBest());
        assertEquals("Caio", scores.getEntries()[0].getName());
        assertEquals("Eva", scores.getEntries()[4].getName());
        assertFalse(scores.add("Gil", 5));
        assertTrue(scores.contains(50));
        assertFalse(scores.contains(10));
    }

    @Test
    void savesAndLoadsNamesAndScores() throws IOException {
        Path file = folder.resolve("s.txt");
        HighScores scores = new HighScores(file);
        scores.add("Ana", 100);
        scores.add("Bia", 40);
        scores.save();

        assertEquals(List.of("Ana;100", "Bia;40"), Files.readAllLines(file));

        HighScores loaded = new HighScores(file);
        assertArrayEquals(new int[] {100, 40, 0, 0, 0}, loaded.getScores());
        assertEquals("Ana", loaded.getEntries()[0].getName());
        assertEquals("Bia", loaded.getEntries()[1].getName());
    }

    @Test
    void ignoresBadLinesInTheFile() throws IOException {
        Path file = folder.resolve("s.txt");
        Files.write(file, List.of("Ana;80", "hello", "Bia;abc", "Caio;-3", "50", " Davi ; 25 "));

        HighScores loaded = new HighScores(file);
        assertArrayEquals(new int[] {80, 25, 0, 0, 0}, loaded.getScores());
        assertEquals("Davi", loaded.getEntries()[1].getName());
    }

    @Test
    void getEntriesReturnsACopy() {
        HighScores scores = new HighScores(folder.resolve("s.txt"), new ScoreEntry("Ana", 10));
        scores.getEntries()[0] = new ScoreEntry("Cheater", 999);
        assertEquals(10, scores.getBest());
    }

    @Test
    void toStringShowsNamesAndScores() {
        HighScores scores = new HighScores(folder.resolve("s.txt"), new ScoreEntry("Ana", 120));
        String text = scores.toString();
        assertTrue(text.startsWith("1. Ana"));
        assertTrue(text.contains("120"));
        assertTrue(text.contains("2. ---"));
    }
}
