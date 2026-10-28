package bughunt.model.actors;

import bughunt.model.core.Direction;
import bughunt.model.core.Match;
import bughunt.model.core.Position;
import bughunt.model.maze.CellType;
import bughunt.model.maze.Maze;
import bughunt.model.modes.BugMode;
import bughunt.model.modes.InBacklog;
import bughunt.model.modes.Look;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic enemy. All the movement lives here; each subclass only says where it
 * wants to go while chasing (computeTarget), and the current mode decides whether
 * the bug chases, scatters, flees or returns to the backlog (State pattern).
 */
public abstract sealed class Bug extends Actor permits NullPointer, RaceCondition, Deadlock, Heisenbug {
    private final String name;
    private final Position scatterCorner;
    private final int releaseOrder;
    private BugMode mode;
    private boolean reverseOnNextStep;

    protected Bug(String name, Position start, Position scatterCorner, int releaseOrder) {
        super(start, Direction.LEFT);
        this.name = name;
        this.scatterCorner = scatterCorner;
        this.releaseOrder = releaseOrder;
    }

    /** The bug's personality: the tile it chases in Chase mode. */
    public abstract Position computeTarget(Match match);

    @Override
    public final void update(Match match) {
        switchTo(mode.update(this, match));
        double speed = mode.speed(this, match);
        if (match.maze().isTunnel(position) && mode.slowsInTunnel()) {
            speed = Math.min(speed, match.config().tunnelBugSpeed());
        }
        advance(speed, match);
    }

    @Override
    protected final boolean step(Match match) {
        Direction chosen = chooseDirection(match);
        if (chosen == null) {
            return false;
        }
        direction = chosen;
        move(match.maze());
        return true;
    }

    /**
     * Template Method: the same algorithm for every bug.
     * On each tile the bug looks at the possible exits (without turning back) and
     * picks the one that brings it closest to the target. With no target, it picks at random.
     */
    protected final Direction chooseDirection(Match match) {
        Maze maze = match.maze();
        boolean access = mode.passesDoor();

        if (reverseOnNextStep) {
            reverseOnNextStep = false;
            Direction opposite = direction.opposite();
            if (maze.isPassable(maze.neighbor(position, opposite), access)) {
                return opposite;
            }
        }

        Position target = mode.target(this, match);
        if (target != null && mode.usesShortestPath()) {
            Direction path = maze.shortestPathDirection(position, target, access);
            if (path != null) {
                return path;
            }
        }

        List<Direction> options = new ArrayList<>(4);
        for (Direction d : Direction.values()) {
            if (d != direction.opposite() && maze.isPassable(maze.neighbor(position, d), access)) {
                options.add(d);
            }
        }
        if (options.isEmpty()) {
            Direction opposite = direction.opposite();
            return maze.isPassable(maze.neighbor(position, opposite), access) ? opposite : null;
        }
        if (target == null) {
            return options.get(match.random().nextInt(options.size()));
        }
        Direction best = options.get(0);
        double shortest = Double.MAX_VALUE;
        for (Direction d : options) {
            double distance = maze.neighbor(position, d).distanceTo(target);
            if (distance < shortest) {
                shortest = distance;
                best = d;
            }
        }
        return best;
    }

    /** Round start: back to the start tile, inside or outside the backlog. */
    @Override
    public void onRoundReset(Match match) {
        returnToStart();
        reverseOnNextStep = false;
        boolean inBacklog = match.maze().cell(start()).type() == CellType.BACKLOG;
        mode = inBacklog
                ? new InBacklog(match.config().releaseDelay(releaseOrder))
                : match.modes().newGlobalMode();
    }

    /** The Debugger touched this bug: the effect depends on the current mode. */
    public void onTouch(Match match) {
        mode.onTouch(this, match);
    }

    public void onWaveChange(BugMode next) {
        switchTo(mode.onWaveChange(this, next));
    }

    public void onBreakpointStart() {
        switchTo(mode.onBreakpointStart(this));
    }

    public void onBreakpointEnd(BugMode global) {
        switchTo(mode.onBreakpointEnd(this, global));
    }

    public void switchTo(BugMode newMode) {
        this.mode = newMode;
    }

    /** Requests a U-turn on the next step (happens when the mode changes). */
    public void reverseDirection() {
        reverseOnNextStep = true;
    }

    /** How the view should draw this bug right now. */
    public Look look(Match match) {
        return mode.look(this, match);
    }

    public BugMode mode() {
        return mode;
    }

    public String name() {
        return name;
    }

    public Position scatterCorner() {
        return scatterCorner;
    }
}
