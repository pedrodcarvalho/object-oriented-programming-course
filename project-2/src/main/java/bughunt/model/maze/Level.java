package bughunt.model.maze;

import bughunt.model.actors.Bug;
import bughunt.model.core.Position;
import bughunt.model.pickups.Pickup;
import java.util.List;

/** Everything the LevelFactory builds from a maze file. */
public record Level(Maze maze, Position debuggerStart, List<Bug> bugs, List<Pickup> pickups) {
}
