package bughunt.model.maze;

import bughunt.model.pickups.Pickup;

/** One maze tile: its type and, optionally, a pickup lying on it. */
public class Cell {
    private final CellType type;
    private Pickup pickup;

    public Cell(CellType type) {
        this.type = type;
    }

    public CellType type() {
        return type;
    }

    /** The pickup on this tile, or null. Collected or expired pickups vanish by themselves. */
    public Pickup pickup() {
        if (pickup != null && !pickup.isActive()) {
            pickup = null;
        }
        return pickup;
    }

    public void place(Pickup newPickup) {
        this.pickup = newPickup;
    }
}
