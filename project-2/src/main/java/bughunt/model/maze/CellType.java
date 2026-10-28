package bughunt.model.maze;

/**
 * Kinds of maze tile.
 * "open" = anyone may pass; "restricted" = only bugs with backlog access may pass.
 */
public enum CellType {
    WALL(false, false),
    CORRIDOR(true, false),
    TUNNEL(true, false),
    DOOR(false, true),
    BACKLOG(false, true);

    private final boolean open;
    private final boolean restricted;

    CellType(boolean open, boolean restricted) {
        this.open = open;
        this.restricted = restricted;
    }

    public boolean isPassable(boolean backlogAccess) {
        return open || (restricted && backlogAccess);
    }
}
