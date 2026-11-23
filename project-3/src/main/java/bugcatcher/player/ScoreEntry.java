package bugcatcher.player;

public class ScoreEntry implements Comparable<ScoreEntry> {
    public static final String SEPARATOR = ";";

    public static final ScoreEntry EMPTY = new ScoreEntry("---", 0);

    private final String name;
    private final int points;

    public ScoreEntry(String name, int points) {
        if (name == null || name.isBlank() || name.contains(SEPARATOR)) {
            throw new IllegalArgumentException("Invalid name for the scoreboard: " + name);
        }
        if (points < 0) {
            throw new IllegalArgumentException("Score cannot be negative, got " + points);
        }
        this.name = name.trim();
        this.points = points;
    }

    public static ScoreEntry fromLine(String line) {
        String[] parts = line.split(SEPARATOR);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected name;points, got " + line);
        }
        return new ScoreEntry(parts[0], Integer.parseInt(parts[1].trim()));
    }

    public String toLine() {
        return name + SEPARATOR + points;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        return Integer.compare(other.points, this.points);
    }

    @Override
    public String toString() {
        return String.format("%s - %d", name, points);
    }
}
