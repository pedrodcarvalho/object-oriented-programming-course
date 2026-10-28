package bughunt.model.core;

import bughunt.model.actors.Bug;
import bughunt.model.actors.Debugger;
import bughunt.model.events.EventBus;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;
import bughunt.model.maze.InvalidMazeException;
import bughunt.model.maze.Level;
import bughunt.model.maze.Maze;
import bughunt.model.maze.MazeLibrary;
import bughunt.model.pickups.Pickup;
import bughunt.model.rules.CollisionManager;
import bughunt.model.rules.LevelConfig;
import bughunt.model.rules.LevelManager;
import bughunt.model.rules.ModeController;
import bughunt.model.rules.ScoreBoard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A match in progress: the maze, the entities and the managers that apply
 * the rules. It is the "world" each entity receives to update itself.
 */
public final class Match {
    private static final int CAPTURE_FREEZE = Ticks.of(0.5);

    private final ScoreBoard score;
    private final EventBus events = new EventBus();
    private final LevelManager levels;
    private final ModeController modes = new ModeController();
    private final CollisionManager collisions = new CollisionManager();
    private final Random random = new Random();
    private final List<Entity> entities = new ArrayList<>();
    private final List<Bug> bugs = new ArrayList<>();

    private Maze maze;
    private Debugger debugger;
    private LevelConfig config;
    private long tick;
    private int frozenTicks;
    private boolean debuggerCaught;
    private boolean levelCleared;

    public Match(MazeLibrary library, ScoreBoard score) throws InvalidMazeException {
        this.score = score;
        this.levels = new LevelManager(library);
        score.newMatch(events);
        levels.connect(this);
        events.subscribe(EventType.BREAKPOINT_EATEN, e -> modes.activateBreakpoint(this));
        events.subscribe(EventType.EXTRA_LIFE, e -> debugger.gainLife());
        events.subscribe(EventType.BUG_CAPTURED, e -> frozenTicks = CAPTURE_FREEZE);
        levels.load(1, this);
    }

    /** Called by the LevelManager with the freshly built level. */
    public void install(Level level, LevelConfig newConfig) {
        this.config = newConfig;
        this.maze = level.maze();
        entities.clear();
        bugs.clear();
        entities.addAll(level.pickups());
        if (debugger == null) {
            debugger = new Debugger(level.debuggerStart());
        } else {
            debugger.setStart(level.debuggerStart());
        }
        entities.add(debugger);
        bugs.addAll(level.bugs());
        entities.addAll(bugs);
        levelCleared = false;
        resetRound();
    }

    /** Level start or after losing a life: each entity knows what to do. */
    public void resetRound() {
        debuggerCaught = false;
        frozenTicks = 0;
        tick = 0;
        modes.start(config);
        for (Entity entity : new ArrayList<>(entities)) {
            entity.onRoundReset(this);
        }
        entities.removeIf(e -> !e.isActive());
        events.publish(new GameEvent(EventType.ROUND_STARTED, debugger.position(), levels.number()));
    }

    /** One game tick: the loop treats every entity the same way. */
    public void tick() {
        if (frozenTicks > 0) {
            frozenTicks--;
            return;
        }
        tick++;
        modes.update(this);
        for (Entity entity : new ArrayList<>(entities)) {
            if (!entity.isActive()) {
                continue;
            }
            entity.update(this);
            collisions.check(this);
            if (debuggerCaught || frozenTicks > 0) {
                break;
            }
        }
        entities.removeIf(e -> !e.isActive());
    }

    public void addPickup(Pickup pickup) {
        maze.cell(pickup.position()).place(pickup);
        entities.add(pickup);
    }

    public void captureDebugger() {
        if (!debuggerCaught) {
            debuggerCaught = true;
            events.publish(new GameEvent(EventType.DEBUGGER_CAUGHT, debugger.position(), 0));
        }
    }

    public void markLevelCleared() {
        levelCleared = true;
        events.publish(new GameEvent(EventType.LEVEL_CLEARED, null, levels.number()));
    }

    public void advanceLevel() throws InvalidMazeException {
        levels.load(levels.number() + 1, this);
    }

    public boolean debuggerWasCaught() {
        return debuggerCaught;
    }

    public boolean levelWasCleared() {
        return levelCleared;
    }

    /** True during the short pause after a bug is captured. */
    public boolean isFrozen() {
        return frozenTicks > 0;
    }

    public Maze maze() {
        return maze;
    }

    public Debugger debugger() {
        return debugger;
    }

    public List<Bug> bugs() {
        return Collections.unmodifiableList(bugs);
    }

    /** The pickups still in the maze. */
    public List<Pickup> pickups() {
        return entities.stream()
                .filter(e -> e instanceof Pickup && e.isActive())
                .map(e -> (Pickup) e)
                .toList();
    }

    public EventBus events() {
        return events;
    }

    public ModeController modes() {
        return modes;
    }

    public ScoreBoard score() {
        return score;
    }

    public LevelConfig config() {
        return config;
    }

    public Random random() {
        return random;
    }

    public long tickCount() {
        return tick;
    }

    public int levelNumber() {
        return levels.number();
    }

    public int pickupsRemaining() {
        return levels.remaining();
    }
}
