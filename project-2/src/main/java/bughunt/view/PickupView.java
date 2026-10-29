package bughunt.view;

import bughunt.model.pickups.Bit;
import bughunt.model.pickups.Breakpoint;
import bughunt.model.pickups.Coffee;
import bughunt.model.pickups.Pickup;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * One pickup lying on its tile: a warm-white solder dot (bit), a red pad that pulses
 * (breakpoint) or a small brown cup (coffee). Pickups never move, so the node is
 * placed once when it is created.
 */
final class PickupView {
    private static final double DIM = 0.35;

    private final Node node;
    private final boolean pulses;
    private final boolean bonus;

    PickupView(Pickup pickup) {
        this.node = switch (pickup) {
            case Bit b -> new Circle(2.5, Theme.BIT);
            case Breakpoint b -> breakpointPad();
            case Coffee c -> coffeeCup();
        };
        this.pulses = pickup instanceof Breakpoint;
        this.bonus = pickup instanceof Coffee;
        node.setTranslateX((pickup.position().x() + 0.5) * Theme.TILE);
        node.setTranslateY((pickup.position().y() + 0.5) * Theme.TILE);
    }

    Node node() {
        return node;
    }

    /**
     * Breakpoints are bright while {@code pulseOn} and dim otherwise. The coffee cup sits on
     * the banner's row, so it hides while a banner is shown (like the arcade's bonus fruit).
     */
    void update(boolean pulseOn, boolean bannerShown) {
        if (pulses) {
            node.setOpacity(pulseOn ? 1.0 : DIM);
        }
        if (bonus) {
            node.setVisible(!bannerShown);
        }
    }

    private static Node breakpointPad() {
        Circle pad = new Circle(7, Theme.BREAKPOINT);
        Circle hole = new Circle(2.5, Theme.BACKGROUND);
        return new Group(pad, hole);
    }

    private static Node coffeeCup() {
        Rectangle cup = new Rectangle(-6, -4, 10, 10);
        cup.setFill(Theme.COFFEE);
        cup.setArcWidth(3);
        cup.setArcHeight(3);
        Rectangle surface = new Rectangle(-5, -4, 8, 2);
        surface.setFill(Theme.COFFEE_SURFACE);
        Arc handle = new Arc(4, 1, 3, 3, -90, 180);
        handle.setType(ArcType.OPEN);
        handle.setFill(null);
        handle.setStroke(Theme.COFFEE);
        handle.setStrokeWidth(2);
        return new Group(cup, surface, handle);
    }
}
