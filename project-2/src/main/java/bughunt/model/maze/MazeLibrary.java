package bughunt.model.maze;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The text of every maze, read once, and the rule that picks a maze for each level:
 * each maze is used for two levels in a row, then the next one, cycling forever.
 */
public final class MazeLibrary {
    public static final List<String> MAZE_FILES = List.of("maze1.maze");
    private static final String FOLDER = "/assets/levels/";

    private final List<List<String>> mazes;

    public MazeLibrary(List<List<String>> mazes) {
        if (mazes.isEmpty()) {
            throw new IllegalArgumentException("A maze library needs at least one maze.");
        }
        this.mazes = mazes.stream().map(List::copyOf).toList();
    }

    /** Reads every file in {@link #MAZE_FILES} from the classpath. */
    public static MazeLibrary fromClasspath() throws InvalidMazeException {
        return fromClasspath(MAZE_FILES);
    }

    static MazeLibrary fromClasspath(List<String> files) throws InvalidMazeException {
        List<List<String>> mazes = new ArrayList<>();
        for (String file : files) {
            mazes.add(read(file));
        }
        return new MazeLibrary(mazes);
    }

    /** Maze index for a level: levels 1-2 use maze 0, levels 3-4 maze 1, and so on, cycling. */
    public static int indexFor(int level, int mazeCount) {
        return ((level - 1) / 2) % mazeCount;
    }

    public List<String> mazeFor(int level) {
        return mazes.get(indexFor(level, mazes.size()));
    }

    public int mazeCount() {
        return mazes.size();
    }

    private static List<String> read(String file) throws InvalidMazeException {
        try (InputStream in = MazeLibrary.class.getResourceAsStream(FOLDER + file)) {
            if (in == null) {
                throw new InvalidMazeException("Maze file not found: " + FOLDER + file);
            }
            List<String> lines = new ArrayList<>(new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList());
            while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
                lines.remove(lines.size() - 1);
            }
            return lines;
        } catch (IOException e) {
            throw new InvalidMazeException("Could not read maze file " + FOLDER + file + ".", e);
        }
    }
}
