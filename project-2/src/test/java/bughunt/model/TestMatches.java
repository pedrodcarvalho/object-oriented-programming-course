package bughunt.model;

import bughunt.model.core.Match;
import bughunt.model.maze.InvalidMazeException;
import bughunt.model.maze.MazeLibrary;
import bughunt.model.rules.ScoreBoard;

/** Shared fixtures for model tests. */
public final class TestMatches {

    private TestMatches() {
    }

    /** A fresh match on maze1 at level 1, with no previous best score. */
    public static Match maze1Match() {
        try {
            return new Match(MazeLibrary.fromClasspath(), new ScoreBoard(0));
        } catch (InvalidMazeException e) {
            throw new IllegalStateException("maze1 must be valid", e);
        }
    }
}
