package bughunt.model.maze;

/** Thrown when a maze file is missing or malformed. */
public class InvalidMazeException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidMazeException(String message) {
        super(message);
    }

    public InvalidMazeException(String message, Throwable cause) {
        super(message, cause);
    }
}
