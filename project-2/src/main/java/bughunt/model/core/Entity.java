package bughunt.model.core;

/**
 * Everything that lives inside the maze: the player, the bugs and the pickups.
 * The match treats every entity the same way (polymorphism): it calls update()
 * without knowing the concrete type of each one.
 */
public abstract class Entity implements Updatable {
    protected Position position;
    private boolean active = true;

    protected Entity(Position position) {
        this.position = position;
    }

    public Position position() {
        return position;
    }

    public boolean isActive() {
        return active;
    }

    /** Marks the entity to be removed from the match at the end of the tick. */
    public void deactivate() {
        active = false;
    }

    /**
     * Called when the round restarts (level start or after losing a life).
     * Does nothing by default; actors go back to their start and temporary items vanish.
     */
    public void onRoundReset(Match match) {
    }
}
