package bugcatcher.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bugcatcher.items.Bug;
import bugcatcher.items.Coffee;
import bugcatcher.items.GoldenCoffee;
import bugcatcher.items.Heart;
import org.junit.jupiter.api.Test;

class PlayerTest {

    @Test
    void startsWithThreeLivesAndNoPoints() {
        Player player = new Player();
        assertEquals("Player", player.getName());
        assertEquals(Player.START_LIVES, player.getLives());
        assertEquals(0, player.getScore());
        assertEquals(1, player.getLevel());
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new Player(" "));
        assertThrows(IllegalArgumentException.class, () -> new Player("Ana", Player.MAX_LIVES + 1));
        Player player = new Player("Ana");
        assertThrows(IllegalArgumentException.class, () -> player.setLives(-1));
        assertThrows(IllegalArgumentException.class, () -> player.addPoints(-5));
    }

    @Test
    void eachItemDoesItsOwnThingWhenCaught() {
        Player player = new Player("Ana");

        player.caught(new Coffee(100));
        assertEquals(10, player.getScore());

        player.caught(new GoldenCoffee(100));
        assertEquals(60, player.getScore());

        player.caught(new Bug(100));
        assertEquals(2, player.getLives());

        player.caught(new Heart(100));
        assertEquals(3, player.getLives());

        assertEquals("2 coffees, 1 bugs, 1 hearts", player.getSummary());
    }

    @Test
    void livesStayBetweenZeroAndMax() {
        Player player = new Player("Ana", Player.MAX_LIVES);
        player.gainLife();
        assertEquals(Player.MAX_LIVES, player.getLives());

        player.loseLife(10);
        assertEquals(0, player.getLives());
        assertFalse(player.isAlive());
    }

    @Test
    void levelGoesUpEveryHundredPoints() {
        Player player = new Player("Ana");
        player.addPoints(99);
        assertEquals(1, player.getLevel());
        player.addPoints(1);
        assertEquals(2, player.getLevel());
        assertTrue(player.toString().contains("level 2"));
    }

    @Test
    void caughtItemsListIsACopy() {
        Player player = new Player("Ana");
        player.caught(new Coffee(100));
        player.getCaughtItems().clear();
        assertEquals(1, player.getCaughtItems().size());
    }
}
