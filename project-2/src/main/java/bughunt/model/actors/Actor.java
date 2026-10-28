package bughunt.model.actors;

import bughunt.model.core.Direction;
import bughunt.model.core.Entity;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.core.Ticks;
import bughunt.model.maze.Maze;

/**
 * Something that walks through the maze, tile by tile.
 * Speed is in tiles per second: each tick the actor accumulates a little
 * "progress" and takes a step whenever the progress reaches 1.
 */
public abstract class Actor extends Entity {
    private Position start;
    private final Direction initialDirection;
    protected Direction direction;
    private double progress;

    protected Actor(Position start, Direction initialDirection) {
        super(start);
        this.start = start;
        this.initialDirection = initialDirection;
        this.direction = initialDirection;
    }

    public Direction direction() {
        return direction;
    }

    /** Fraction of the way to the next tile, in [0, 1). */
    public double progress() {
        return progress;
    }

    public Position start() {
        return start;
    }

    public void setStart(Position newStart) {
        this.start = newStart;
    }

    public void returnToStart() {
        position = start;
        direction = initialDirection;
        progress = 0;
    }

    /** Accumulates this tick's movement and takes a step for every whole tile. */
    protected void advance(double tilesPerSecond, Match match) {
        progress += tilesPerSecond / Ticks.PER_SECOND;
        while (progress >= 1.0) {
            progress -= 1.0;
            if (!step(match)) {
                progress = 0;
                return;
            }
        }
    }

    /** Decides the direction and walks one tile. Returns false if the actor stayed put. */
    protected abstract boolean step(Match match);

    protected void move(Maze maze) {
        position = maze.neighbor(position, direction);
    }
}
