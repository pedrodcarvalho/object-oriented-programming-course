package bughunt.view;

import bughunt.model.actors.Bug;
import bughunt.model.actors.Deadlock;
import bughunt.model.actors.Heisenbug;
import bughunt.model.actors.NullPointer;
import bughunt.model.actors.RaceCondition;
import bughunt.model.core.Direction;
import bughunt.model.modes.Look;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;

/**
 * One bug: a beetle silhouette (body, head and six legs) facing its direction of travel,
 * in its own colour, blue while vulnerable and white while flashing. A captured bug is
 * just a pair of eyes heading back to the backlog.
 */
final class BugView {
    private static final double LEG_WIDTH = 1.6;
    /** Leg roots along the body (x) and where each leg ends (x), drawn pointing right. */
    private static final double[][] LEGS = {{-6, -9}, {-2, -2}, {2, 5}};

    private final Group root = new Group();
    private final Group beetle = new Group();
    private final Group eyes = new Group();
    private final Rotate heading = new Rotate(0, 0, 0);
    private final Color colour;
    private final List<Shape> painted = new ArrayList<>();
    private Color current;

    BugView(Bug bug) {
        this.colour = colourOf(bug);

        Ellipse body = new Ellipse(-2, 0, 7, 6);
        Circle head = new Circle(7, 0, 3.5);
        Line wings = new Line(-8, 0, 3, 0);
        wings.setStroke(Theme.BACKGROUND);
        wings.setStrokeWidth(1);
        painted.add(body);
        painted.add(head);
        for (double[] leg : LEGS) {
            for (int side : new int[] {-1, 1}) {
                Line line = new Line(leg[0], side * 4, leg[1], side * 10);
                line.setStrokeWidth(LEG_WIDTH);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                painted.add(line);
                beetle.getChildren().add(line);
            }
        }
        beetle.getChildren().addAll(body, head, wings);

        for (int side : new int[] {-1, 1}) {
            eyes.getChildren().addAll(
                    new Circle(2, side * 4, 3.5, Theme.FLASH),
                    new Circle(3.5, side * 4, 1.6, Theme.PUPIL));
        }

        Group sprite = new Group(beetle, eyes);
        sprite.getTransforms().add(heading);
        root.getChildren().add(sprite);
        paint(colour);
    }

    Node node() {
        return root;
    }

    /** Draws the bug centred at (x, y) px, facing {@code facing}, the way {@code look} says. */
    void show(double x, double y, Direction facing, Look look) {
        root.setVisible(true);
        root.setTranslateX(x);
        root.setTranslateY(y);
        heading.setAngle(DebuggerView.angle(facing));
        beetle.setVisible(look != Look.EYES);
        eyes.setVisible(look == Look.EYES);
        switch (look) {
            case NORMAL -> paint(colour);
            case VULNERABLE -> paint(Theme.VULNERABLE);
            case FLASHING -> paint(Theme.FLASH);
            case EYES -> { }
        }
    }

    void hide() {
        root.setVisible(false);
    }

    /** Each bug's signature colour. */
    static Color colourOf(Bug bug) {
        return switch (bug) {
            case NullPointer n -> Theme.NULL_POINTER;
            case RaceCondition r -> Theme.RACE_CONDITION;
            case Deadlock d -> Theme.DEADLOCK;
            case Heisenbug h -> Theme.HEISENBUG;
        };
    }

    private void paint(Color newColour) {
        if (newColour.equals(current)) {
            return;
        }
        current = newColour;
        for (Shape shape : painted) {
            if (shape instanceof Line) {
                shape.setStroke(newColour);
            } else {
                shape.setFill(newColour);
            }
        }
    }
}
