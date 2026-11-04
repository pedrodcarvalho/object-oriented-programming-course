package bugcatcher.items;

/**
 * Something that hurts the player when caught.
 * An interface can extend another interface: every Dangerous item is also Catchable.
 */
public interface Dangerous extends Catchable {

    /** How many lives the player loses. */
    int getDamage();
}
