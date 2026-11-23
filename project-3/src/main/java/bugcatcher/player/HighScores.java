package bugcatcher.player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HighScores {
    public static final int SIZE = 5;

    private final ScoreEntry[] entries = new ScoreEntry[SIZE];
    private final Path file;

    public HighScores(Path file) {
        this.file = file;
        Arrays.fill(entries, ScoreEntry.EMPTY);
        load();
    }

    public HighScores(Path file, ScoreEntry... initialEntries) {
        this(file);
        for (ScoreEntry entry : initialEntries) {
            add(entry);
        }
    }

    public boolean add(String name, int points) {
        return add(new ScoreEntry(name, points));
    }

    public boolean add(ScoreEntry entry) {
        if (entry.getPoints() <= entries[SIZE - 1].getPoints()) {
            return false;
        }

        ScoreEntry[] all = new ScoreEntry[SIZE + 1];
        System.arraycopy(entries, 0, all, 0, SIZE);
        all[SIZE] = entry;
        Arrays.sort(all);

        System.arraycopy(all, 0, entries, 0, SIZE);
        return true;
    }

    public int getBest() {
        return entries[0].getPoints();
    }

    public ScoreEntry[] getEntries() {
        return Arrays.copyOf(entries, entries.length);
    }

    public int[] getScores() {
        int[] scores = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            scores[i] = entries[i].getPoints();
        }
        return scores;
    }

    public boolean contains(int score) {
        int[] sorted = getScores();
        Arrays.sort(sorted);
        return Arrays.binarySearch(sorted, score) >= 0;
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (ScoreEntry entry : entries) {
            if (entry != ScoreEntry.EMPTY) {
                lines.add(entry.toLine());
            }
        }
        try {
            Files.write(file, lines);
        } catch (IOException e) {
            System.err.println("Could not save high scores: " + e.getMessage());
        }
    }

    private void load() {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (NoSuchFileException e) {
            return;
        } catch (IOException e) {
            System.err.println("Could not read high scores: " + e.getMessage());
            return;
        }

        for (String line : lines) {
            try {
                add(ScoreEntry.fromLine(line));
            } catch (IllegalArgumentException e) {
                System.err.println("Ignoring bad high score line: " + line);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < entries.length; i++) {
            text.append(String.format("%d. %s%n", i + 1, entries[i]));
        }
        return text.toString();
    }
}
