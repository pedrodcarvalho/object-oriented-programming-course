package bugcatcher.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScoreEntryTest {
    @Test
    void writesAndReadsALine() {
        ScoreEntry entry = new ScoreEntry("Ana", 120);
        assertEquals("Ana;120", entry.toLine());

        ScoreEntry read = ScoreEntry.fromLine("Ana;120");
        assertEquals("Ana", read.getName());
        assertEquals(120, read.getPoints());
    }

    @Test
    void biggerScoreComesFirst() {
        ScoreEntry high = new ScoreEntry("Ana", 120);
        ScoreEntry low = new ScoreEntry("Bia", 40);
        assertTrue(high.compareTo(low) < 0);
        assertTrue(low.compareTo(high) > 0);
    }

    @Test
    void rejectsBadValues() {
        assertThrows(IllegalArgumentException.class, () -> new ScoreEntry(" ", 10));
        assertThrows(IllegalArgumentException.class, () -> new ScoreEntry("A;B", 10));
        assertThrows(IllegalArgumentException.class, () -> new ScoreEntry("Ana", -1));
        assertThrows(IllegalArgumentException.class, () -> ScoreEntry.fromLine("120"));
        assertThrows(IllegalArgumentException.class, () -> ScoreEntry.fromLine("Ana;abc"));
    }
}
