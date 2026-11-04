package bugcatcher.items;

/**
 * Creates random items. It only has static methods, so it is never instantiated
 * (that is why the constructor is private).
 */
public final class ItemFactory {

    public static final double BASE_SPEED = 160;   // pixels per second at level 1
    public static final double SPEED_PER_LEVEL = 30;

    private ItemFactory() {
    }

    /** Picks a random item. Higher levels make everything fall faster. */
    public static FallingItem randomItem(int level) {
        int roll = (int) (Math.random() * 100); // casting: double 0..99.99 -> int 0..99
        return randomItem(level, roll);
    }

    /**
     * Overloaded version where we choose the "dice roll" (0 to 99).
     * Out of 100 items: 50 coffees, 10 golden coffees, 37 bugs and 3 hearts.
     */
    public static FallingItem randomItem(int level, int roll) {
        if (level < 1) {
            throw new IllegalArgumentException("Level starts at 1, got " + level);
        }
        if (roll < 0 || roll > 99) {
            throw new IllegalArgumentException("Roll must be between 0 and 99, got " + roll);
        }

        // (level - 1) is an int; Java promotes it to double to multiply by SPEED_PER_LEVEL
        double speed = BASE_SPEED + (level - 1) * SPEED_PER_LEVEL;

        if (roll < 50) {
            return new Coffee(speed);
        } else if (roll < 60) {
            return new GoldenCoffee(speed);
        } else if (roll < 97) {
            return new Bug(speed);
        } else {
            return new Heart(speed);
        }
    }
}
