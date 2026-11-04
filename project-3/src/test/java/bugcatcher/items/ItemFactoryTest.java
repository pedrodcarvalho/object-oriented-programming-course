package bugcatcher.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ItemFactoryTest {

    @Test
    void rollDecidesTheKindOfItem() {
        assertInstanceOf(Coffee.class, ItemFactory.randomItem(1, 0));
        assertInstanceOf(GoldenCoffee.class, ItemFactory.randomItem(1, 55));
        assertInstanceOf(Bug.class, ItemFactory.randomItem(1, 80));
        assertInstanceOf(Heart.class, ItemFactory.randomItem(1, 99));
    }

    @Test
    void higherLevelsFallFaster() {
        FallingItem slow = ItemFactory.randomItem(1, 80);
        FallingItem fast = ItemFactory.randomItem(3, 80);
        assertEquals(ItemFactory.BASE_SPEED, slow.getSpeed());
        assertEquals(ItemFactory.BASE_SPEED + 2 * ItemFactory.SPEED_PER_LEVEL, fast.getSpeed());
    }

    @Test
    void rejectsBadArguments() {
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.randomItem(0, 10));
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.randomItem(1, 100));
        assertThrows(IllegalArgumentException.class, () -> new Bug(0));
    }

    @Test
    void goldenCoffeeIsWorthMoreAndFallsFaster() {
        Coffee normal = new Coffee(100);
        Coffee golden = new GoldenCoffee(100); // a GoldenCoffee "is a" Coffee
        assertEquals(10, normal.getPoints());
        assertEquals(50, golden.getPoints());
        assertEquals(150, golden.getSpeed());
        assertTrue(golden.toString().startsWith("Golden Coffee"));
    }

    @Test
    void countsCreatedItems() {
        int before = FallingItem.getCreatedCount();
        new Coffee(100);
        new Bug(100);
        assertEquals(before + 2, FallingItem.getCreatedCount());
    }
}
