package bughunt.model.actors;

import bughunt.model.core.Direction;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.maze.Maze;

/**
 * The player: the debugger's yellow execution arrow.
 * It remembers the last requested turn and takes it at the first junction where it is possible.
 */
public class Debugger extends Actor {
    public static final int LIVES_AT_START = 3;

    private Direction requestedDirection;
    private int lives = LIVES_AT_START;

    public Debugger(Position start) {
        super(start, Direction.LEFT);
        this.requestedDirection = Direction.LEFT;
    }

    public void requestDirection(Direction newDirection) {
        this.requestedDirection = newDirection;
    }

    public int lives() {
        return lives;
    }

    public boolean hasLives() {
        return lives > 0;
    }

    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    public void gainLife() {
        lives++;
    }

    @Override
    public void update(Match match) {
        advance(match.config().debuggerSpeed(), match);
    }

    @Override
    protected boolean step(Match match) {
        Maze maze = match.maze();
        if (maze.isPassable(maze.neighbor(position, requestedDirection), false)) {
            direction = requestedDirection;
        }
        if (!maze.isPassable(maze.neighbor(position, direction), false)) {
            return false;
        }
        move(maze);
        return true;
    }

    @Override
    public void returnToStart() {
        super.returnToStart();
        requestedDirection = direction();
    }

    @Override
    public void onRoundReset(Match match) {
        returnToStart();
    }
}
