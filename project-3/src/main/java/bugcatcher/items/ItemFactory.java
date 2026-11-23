package bugcatcher.items;

public final class ItemFactory {
    public static final double BASE_SPEED = 160;
    public static final double SPEED_PER_LEVEL = 30;

    private ItemFactory() {
    }

    public static FallingItem randomItem(int level) {
        int roll = (int) (Math.random() * 100);
        return randomItem(level, roll);
    }

    public static FallingItem randomItem(int level, int roll) {
        if (level < 1) {
            throw new IllegalArgumentException("Level starts at 1, got " + level);
        }
        if (roll < 0 || roll > 99) {
            throw new IllegalArgumentException("Roll must be between 0 and 99, got " + roll);
        }

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
