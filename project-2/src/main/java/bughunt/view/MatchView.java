package bughunt.view;

import bughunt.model.actors.Actor;
import bughunt.model.actors.Bug;
import bughunt.model.core.Direction;
import bughunt.model.core.Match;
import bughunt.model.core.Ticks;
import bughunt.model.flow.GamePhase;
import bughunt.model.flow.LevelCleared;
import bughunt.model.flow.LifeLost;
import bughunt.model.flow.MatchSession;
import bughunt.model.flow.PhaseKind;
import bughunt.model.maze.Maze;
import bughunt.model.pickups.Pickup;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * The match as the player sees it: a plain JavaFX node tree that reads the session every
 * frame and draws the maze, the pickups, the debugger, the bugs and the phase banner
 * ("READY!", "SEGFAULT", ...). It only reads the model, never changes it.
 *
 * <p>Static geometry is rebuilt only when the maze instance changes (a new level); each
 * frame merely moves and re-styles the actor and pickup nodes. Actors are placed by an
 * {@link ActorTrail} each, which trails the model by up to one tile so they never cut corners.
 */
public final class MatchView {
    /** The death spin turns the cursor a quarter turn this often. */
    private static final int SPIN_STEP = Ticks.of(0.08);
    /** Breakpoints switch between bright and dim this often, in seconds. */
    private static final double PULSE_PERIOD = 0.25;
    private static final double BANNER_SIZE = 22;

    private final MatchSession session;
    private final Pane root = new Pane();
    private final Group mazeLayer = new Group();
    private final Group pickupLayer = new Group();
    private final Group bugLayer = new Group();
    private final DebuggerView debuggerView = new DebuggerView();
    private final Text banner = new Text();
    private final Map<Pickup, PickupView> pickupViews = new IdentityHashMap<>();
    private final Map<Bug, BugView> bugViews = new IdentityHashMap<>();
    private final ActorTrail debuggerTrail = new ActorTrail();
    private final Map<Bug, ActorTrail> bugTrails = new IdentityHashMap<>();

    private Maze maze;
    private MazeRenderer mazeRenderer;
    /** Seconds driving the breakpoint pulse; it holds still while a life is lost (scene frozen). */
    private double pulseClock;

    public MatchView(MatchSession session) {
        this.session = session;
        banner.setFont(Theme.title(BANNER_SIZE));
        banner.setTextOrigin(VPos.CENTER);
        banner.setVisible(false);
        root.getChildren().addAll(mazeLayer, pickupLayer, debuggerView.node(), bugLayer, banner);
        update(0);
    }

    /** The view's node, sized to the maze in pixels; place it at {@code Theme.MAZE_X/Y}. */
    public Pane root() {
        return root;
    }

    /** Called every frame: brings every node in line with the current model state. */
    public void update(double tpf) {
        Match match = session.match();
        GamePhase phase = session.phase();
        if (match.maze() != maze) {
            rebuildMaze(match.maze());
        }
        if (phase.kind() != PhaseKind.LIFE_LOST) {
            pulseClock += tpf;
        }
        boolean bannerShown = drawBanner(phase);
        syncPickups(match, bannerShown);
        drawDebugger(match, phase);
        drawBugs(match, phase);
        mazeRenderer.setFlash(phase instanceof LevelCleared cleared && cleared.mazeFlashOn());
    }

    private void rebuildMaze(Maze newMaze) {
        maze = newMaze;
        mazeRenderer = new MazeRenderer(newMaze);
        mazeLayer.getChildren().setAll(mazeRenderer.node());
        double width = newMaze.width() * Theme.TILE;
        double height = newMaze.height() * Theme.TILE;
        root.setMinSize(width, height);
        root.setPrefSize(width, height);
        root.setMaxSize(width, height);
    }

    /**
     * Adds nodes for new pickups (coffee, a new level), drops those that were collected and
     * pulses the breakpoints. The coffee shares the banner's row, so it hides behind a banner.
     */
    private void syncPickups(Match match, boolean bannerShown) {
        List<Pickup> pickups = match.pickups();
        Set<Pickup> live = Collections.newSetFromMap(new IdentityHashMap<>());
        live.addAll(pickups);
        Iterator<Map.Entry<Pickup, PickupView>> it = pickupViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Pickup, PickupView> entry = it.next();
            if (!live.contains(entry.getKey())) {
                pickupLayer.getChildren().remove(entry.getValue().node());
                it.remove();
            }
        }
        boolean pulseOn = ((long) (pulseClock / PULSE_PERIOD)) % 2 == 0;
        for (Pickup pickup : pickups) {
            PickupView view = pickupViews.computeIfAbsent(pickup, p -> {
                PickupView created = new PickupView(p);
                pickupLayer.getChildren().add(created.node());
                return created;
            });
            view.update(pulseOn, bannerShown);
        }
    }

    /**
     * The cursor as usual; while a life is being lost it stays frozen, then spins, then
     * crashes into a glitched "X". After game over it is gone.
     */
    private void drawDebugger(Match match, GamePhase phase) {
        Point2D centre = follow(match.debugger(), debuggerTrail, phase);
        double x = centre.getX();
        double y = centre.getY();
        Direction facing = debuggerTrail.facing();
        int elapsed = phase.elapsedTicks();
        switch (phase.kind()) {
            case GAME_OVER -> debuggerView.hide();
            case LIFE_LOST -> {
                if (elapsed < LifeLost.FROZEN) {
                    debuggerView.showCursor(x, y, facing, 0);
                } else if (elapsed < LifeLost.SPINNING) {
                    debuggerView.showCursor(x, y, facing, (elapsed - LifeLost.FROZEN) / SPIN_STEP);
                } else {
                    debuggerView.showCrash(x, y);
                }
            }
            default -> debuggerView.showCursor(x, y, facing, 0);
        }
    }

    /** Keeps one view and trail per bug of the current level and draws each the way its mode says. */
    private void drawBugs(Match match, GamePhase phase) {
        List<Bug> bugs = match.bugs();
        if (bugViews.size() != bugs.size() || !bugs.stream().allMatch(bugViews::containsKey)) {
            bugViews.clear();
            bugTrails.clear();
            bugLayer.getChildren().clear();
            for (Bug bug : bugs) {
                BugView view = new BugView(bug);
                bugViews.put(bug, view);
                bugTrails.put(bug, new ActorTrail());
                bugLayer.getChildren().add(view.node());
            }
        }
        boolean visible = bugsVisible(phase);
        for (Bug bug : bugs) {
            ActorTrail trail = bugTrails.get(bug);
            Point2D centre = follow(bug, trail, phase);
            BugView view = bugViews.get(bug);
            if (visible) {
                view.show(centre.getX(), centre.getY(), trail.facing(), bug.look(match));
            } else {
                view.hide();
            }
        }
    }

    /** Bugs vanish while the cursor spins and crashes, once the cleared maze flashes, and at game over. */
    private static boolean bugsVisible(GamePhase phase) {
        int elapsed = phase.elapsedTicks();
        return switch (phase.kind()) {
            case LIFE_LOST -> elapsed < LifeLost.FROZEN;
            case LEVEL_CLEARED -> elapsed <= LevelCleared.STILL;
            case GAME_OVER -> false;
            case READY, PLAYING -> true;
        };
    }

    /** The phase banner, centred on the coffee row like the arcade's "READY!". Returns whether it is shown. */
    private boolean drawBanner(GamePhase phase) {
        String text = null;
        Color colour = null;
        switch (phase.kind()) {
            case READY -> {
                text = "READY!";
                colour = Theme.DEBUGGER;
            }
            case LIFE_LOST -> {
                if (phase.elapsedTicks() >= LifeLost.SPINNING) {
                    text = "SEGFAULT";
                    colour = Theme.CRASH;
                }
            }
            case LEVEL_CLEARED -> {
                text = "BUILD PASSED";
                colour = Theme.TRACE;
            }
            case GAME_OVER -> {
                text = "GAME OVER";
                colour = Theme.CRASH;
            }
            case PLAYING -> { }
        }
        if (text == null) {
            banner.setVisible(false);
            return false;
        }
        if (!text.equals(banner.getText())) {
            banner.setText(text);
        }
        banner.setFill(colour);
        banner.setX(maze.width() * Theme.TILE / 2.0 - banner.getLayoutBounds().getWidth() / 2);
        banner.setY((maze.coffeeSpot().y() + 0.5) * Theme.TILE);
        banner.setVisible(true);
        return true;
    }

    /**
     * Feeds this frame's actor state to its trail and returns the centre to draw at, in px.
     * During READY every actor stands on its start tile, so the trail snaps there instead
     * of sliding in from where the last round ended.
     */
    private static Point2D follow(Actor actor, ActorTrail trail, GamePhase phase) {
        boolean snap = phase.kind() == PhaseKind.READY;
        trail.follow(actor.position(), actor.direction(), actor.progress(), snap);
        return new Point2D((trail.x() + 0.5) * Theme.TILE, (trail.y() + 0.5) * Theme.TILE);
    }
}
