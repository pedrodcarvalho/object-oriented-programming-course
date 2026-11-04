package bugcatcher.items;

/** Something that is worth points when caught. */
public interface Scorable {

    /** Constant in an interface: it is automatically public, static and final. */
    int BASE_POINTS = 10;

    int getPoints();
}
