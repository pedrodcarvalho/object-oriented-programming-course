package bugcatcher.player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The top 5 scores, kept in an int array (biggest first) and saved to a text file
 * with one score per line.
 */
public class HighScores {

    public static final int SIZE = 5;

    private final int[] scores = new int[SIZE];
    private final Path file;

    /** Loads the scores saved in the file (if there is one). */
    public HighScores(Path file) {
        this.file = file;
        Arrays.fill(scores, 0); // new int arrays already start at 0, but now it's explicit
        load();
    }

    /** Overloaded constructor that also adds some scores right away (varargs). */
    public HighScores(Path file, int... initialScores) {
        this(file);
        for (int score : initialScores) {
            add(score);
        }
    }

    /**
     * Adds a score; it stays in the table only if it's one of the 5 best.
     * @return true if the score entered the table
     */
    public boolean add(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative, got " + score);
        }
        if (score <= scores[SIZE - 1]) {
            return false; // not better than the 5th place
        }

        // Copy the 5 scores into a bigger array, put the new one at the end and sort.
        int[] all = new int[SIZE + 1];
        System.arraycopy(scores, 0, all, 0, SIZE);
        all[SIZE] = score;
        Arrays.sort(all); // ascending: the smallest is at index 0 and gets dropped

        // Copy back the 5 biggest, from the biggest to the smallest.
        for (int i = 0; i < SIZE; i++) {
            scores[i] = all[SIZE - i];
        }
        return true;
    }

    public int getBest() {
        return scores[0];
    }

    /** A copy of the array: arrays are references, so returning ours would expose it. */
    public int[] getScores() {
        return Arrays.copyOf(scores, scores.length);
    }

    /** Is this score in the table? Uses binary search, which needs a sorted array. */
    public boolean contains(int score) {
        int[] sorted = getScores();
        Arrays.sort(sorted);
        return Arrays.binarySearch(sorted, score) >= 0;
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (int score : scores) {
            lines.add(String.valueOf(score));
        }
        try {
            Files.write(file, lines);
        } catch (IOException e) {
            // Not being able to save is not a reason to crash the game.
            System.err.println("Could not save high scores: " + e.getMessage());
        }
    }

    private void load() {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (NoSuchFileException e) {
            return; // first time playing: no file yet
        } catch (IOException e) {
            System.err.println("Could not read high scores: " + e.getMessage());
            return;
        }

        for (String line : lines) {
            try {
                add(Integer.parseInt(line.trim()));
            } catch (IllegalArgumentException e) {
                // NumberFormatException is a subclass of IllegalArgumentException,
                // so this catches both "abc" and negative numbers. Skip that line.
                System.err.println("Ignoring bad high score line: " + line);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < scores.length; i++) {
            text.append(String.format("%d. %d%n", i + 1, scores[i]));
        }
        return text.toString();
    }
}
