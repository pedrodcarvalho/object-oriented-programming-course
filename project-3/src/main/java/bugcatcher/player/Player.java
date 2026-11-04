package bugcatcher.player;

import bugcatcher.items.Bug;
import bugcatcher.items.FallingItem;
import bugcatcher.items.Heart;
import java.util.ArrayList;

/**
 * The person playing: name, score, lives and everything they caught.
 * All fields are private (encapsulation); the outside world uses the methods.
 */
public class Player {

    public static final int START_LIVES = 3;
    public static final int MAX_LIVES = 5;
    public static final int POINTS_PER_LEVEL = 100;

    private final String name;
    private int score;
    private int lives;
    private final ArrayList<FallingItem> caughtItems = new ArrayList<>();

    /** Default constructor: an anonymous player. */
    public Player() {
        this("Player");
    }

    public Player(String name) {
        this(name, START_LIVES);
    }

    public Player(String name, int lives) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The player needs a name");
        }
        this.name = name;
        setLives(lives); // reuse the validation of the setter
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        if (lives < 0 || lives > MAX_LIVES) {
            throw new IllegalArgumentException("Lives must be between 0 and " + MAX_LIVES + ", got " + lives);
        }
        this.lives = lives; // "this" tells the field apart from the parameter with the same name
    }

    public void addPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative, got " + points);
        }
        score += points;
    }

    public void loseLife(int amount) {
        setLives(Math.max(0, lives - amount)); // never below zero
    }

    public void gainLife() {
        setLives(Math.min(MAX_LIVES, lives + 1)); // never above the maximum
    }

    public boolean isAlive() {
        return lives > 0;
    }

    /** Level 1 from 0 to 99 points, level 2 from 100 to 199... (integer division). */
    public int getLevel() {
        return 1 + score / POINTS_PER_LEVEL;
    }

    /**
     * Called when the basket touches an item. The player doesn't need to know
     * which kind of item it is: polymorphism picks the right onCaught().
     */
    public void caught(FallingItem item) {
        caughtItems.add(item);
        item.onCaught(this);
    }

    /** A copy, so nobody outside can change our list. */
    public ArrayList<FallingItem> getCaughtItems() {
        return new ArrayList<>(caughtItems);
    }

    /** For example: "12 coffees, 3 bugs, 1 hearts". */
    public String getSummary() {
        int coffees = 0;
        int bugs = 0;
        int hearts = 0;
        for (FallingItem item : caughtItems) { // enhanced for (for-each)
            if (item instanceof Bug) {
                bugs++;
            } else if (item instanceof Heart) {
                hearts++;
            } else {
                coffees++;
            }
        }
        return String.format("%d coffees, %d bugs, %d hearts", coffees, bugs, hearts);
    }

    @Override
    public String toString() {
        return String.format("%s: %d points, %d lives, level %d", name, score, lives, getLevel());
    }
}
