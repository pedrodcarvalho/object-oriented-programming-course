package bughunt.model.rules;

import bughunt.model.core.Match;
import bughunt.model.events.EventType;
import bughunt.model.events.GameEvent;
import bughunt.model.maze.InvalidMazeException;
import bughunt.model.maze.Level;
import bughunt.model.maze.LevelFactory;
import bughunt.model.maze.MazeLibrary;
import bughunt.model.pickups.Coffee;
import java.util.List;

/**
 * Builds each level from its maze, applies the level's configuration, makes the
 * coffee appear and notices when the build passed (everything collected).
 */
public class LevelManager {
    /** Pickup counts at which a coffee appears. */
    public static final List<Integer> COFFEE_AT = List.of(70, 170);

    private final LevelFactory factory = new LevelFactory();
    private final MazeLibrary library;
    private int number;
    private int remaining;
    private int collected;
    private LevelConfig config;

    public LevelManager(MazeLibrary library) {
        this.library = library;
    }

    public void connect(Match match) {
        match.events().subscribe(EventType.BIT_EATEN, e -> onCollect(match));
        match.events().subscribe(EventType.BREAKPOINT_EATEN, e -> onCollect(match));
    }

    public void load(int levelNumber, Match match) throws InvalidMazeException {
        Level level = factory.create(library.mazeFor(levelNumber));
        number = levelNumber;
        config = LevelConfig.forLevel(levelNumber);
        remaining = level.pickups().size();
        collected = 0;
        match.install(level, config);
    }

    private void onCollect(Match match) {
        remaining--;
        collected++;
        if (COFFEE_AT.contains(collected)) {
            Coffee coffee = new Coffee(match.maze().coffeeSpot(), config.coffeeValue());
            match.addPickup(coffee);
            match.events().publish(new GameEvent(EventType.COFFEE_SPAWNED, coffee.position(), coffee.points()));
        }
        if (remaining == 0) {
            match.markLevelCleared();
        }
    }

    public int number() {
        return number;
    }

    public int remaining() {
        return remaining;
    }

    public LevelConfig config() {
        return config;
    }
}
