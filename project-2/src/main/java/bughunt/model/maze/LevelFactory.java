package bughunt.model.maze;

import bughunt.model.actors.Bug;
import bughunt.model.actors.Deadlock;
import bughunt.model.actors.Heisenbug;
import bughunt.model.actors.NullPointer;
import bughunt.model.actors.RaceCondition;
import bughunt.model.core.Direction;
import bughunt.model.core.Position;
import bughunt.model.pickups.Bit;
import bughunt.model.pickups.Breakpoint;
import bughunt.model.pickups.Pickup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a level from the text of a maze file (Factory pattern).
 *
 * File legend:
 *   #  wall            .  bit             o  breakpoint
 *   (space) corridor   T  tunnel          -  backlog door
 *   H  backlog         P  Debugger start
 *   C  where the coffee appears
 *   1  NullPointer (outside the backlog)  2, 3, 4  RaceCondition, Deadlock, Heisenbug (in the backlog)
 */
public class LevelFactory {

    public Level create(List<String> lines) throws InvalidMazeException {
        if (lines.isEmpty()) {
            throw new InvalidMazeException("The maze file is empty.");
        }
        int height = lines.size();
        int width = lines.get(0).length();
        Cell[][] cells = new Cell[height][width];
        List<Pickup> pickups = new ArrayList<>();
        Map<Character, Position> bugStarts = new HashMap<>();
        List<Position> doors = new ArrayList<>();
        Position debuggerStart = null;
        Position coffeeSpot = null;

        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            if (line.length() != width) {
                throw new InvalidMazeException("Line " + (y + 1) + " has " + line.length()
                        + " characters; all lines must have " + width + ".");
            }
            for (int x = 0; x < width; x++) {
                char c = line.charAt(x);
                Position p = new Position(x, y);
                CellType type = switch (c) {
                    case '#' -> CellType.WALL;
                    case '.', 'o', ' ', 'P', 'C', '1' -> CellType.CORRIDOR;
                    case 'T' -> CellType.TUNNEL;
                    case '-' -> CellType.DOOR;
                    case 'H', '2', '3', '4' -> CellType.BACKLOG;
                    default -> throw new InvalidMazeException("Unknown character '" + c
                            + "' at line " + (y + 1) + ", column " + (x + 1) + ".");
                };
                cells[y][x] = new Cell(type);
                switch (c) {
                    case '.' -> pickups.add(new Bit(p));
                    case 'o' -> pickups.add(new Breakpoint(p));
                    case 'P' -> debuggerStart = p;
                    case 'C' -> coffeeSpot = p;
                    case '-' -> doors.add(p);
                    case '1', '2', '3', '4' -> {
                        if (bugStarts.put(c, p) != null) {
                            throw new InvalidMazeException("Bug '" + c + "' appears more than once in the maze.");
                        }
                    }
                    default -> { }
                }
            }
        }

        require(debuggerStart != null, "Missing the Debugger start (P).");
        require(coffeeSpot != null, "Missing the coffee spot (C).");
        require(!doors.isEmpty(), "Missing the backlog door (-).");
        for (char bug = '1'; bug <= '4'; bug++) {
            require(bugStarts.containsKey(bug), "Missing bug '" + bug + "'.");
        }

        Position door = doors.get(0);
        Maze maze = new Maze(cells, door.move(Direction.UP), door.move(Direction.DOWN), coffeeSpot);
        for (Pickup pickup : pickups) {
            maze.cell(pickup.position()).place(pickup);
        }

        // Scatter corners lie outside the map, as in the original: each bug "aims" at a corner.
        NullPointer nullPointer = new NullPointer(bugStarts.get('1'), new Position(width - 3, -2));
        RaceCondition raceCondition = new RaceCondition(bugStarts.get('2'), new Position(2, -2));
        Deadlock deadlock = new Deadlock(bugStarts.get('3'), new Position(width - 1, height + 1), nullPointer);
        Heisenbug heisenbug = new Heisenbug(bugStarts.get('4'), new Position(0, height + 1));
        List<Bug> bugs = List.of(nullPointer, raceCondition, deadlock, heisenbug);

        return new Level(maze, debuggerStart, bugs, pickups);
    }

    private static void require(boolean condition, String message) throws InvalidMazeException {
        if (!condition) {
            throw new InvalidMazeException(message);
        }
    }
}
