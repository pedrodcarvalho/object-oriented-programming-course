package bughunt;

import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getGameTimer;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.getPrimaryStage;

import bughunt.model.core.FixedStepClock;
import bughunt.model.core.Ticks;
import bughunt.model.flow.MatchSession;
import bughunt.model.maze.InvalidMazeException;
import bughunt.model.maze.MazeLibrary;
import bughunt.qa.QaShots;
import bughunt.ui.ErrorDialog;
import bughunt.ui.Hud;
import bughunt.view.MatchView;
import bughunt.view.Theme;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.logging.Logger;
import java.nio.file.Path;
import javafx.util.Duration;

/**
 * The FXGL application: FXGL's default menus around one {@link MatchSession} per game.
 * Each frame it turns the frame time into fixed model ticks, then refreshes the view and HUD.
 * Every "New Game" builds a fresh session, view and HUD; only the session best score survives.
 */
public class BugHuntApp extends GameApplication {
    /** Program argument that turns on the scripted screenshot run, e.g. {@code --qa-shots=target/qa-shots}. */
    private static final String QA_SHOTS_ARG = "--qa-shots=";
    /**
     * FXGL builds the app instance itself, so {@link #main} hands the screenshot folder
     * over through this system property.
     */
    private static final String QA_SHOTS_PROPERTY = "bughunt.qa.shots";
    private static final int MAX_TICKS_PER_FRAME = 5;
    private static final Duration GAME_OVER_HOLD = Duration.seconds(3);
    private static final Logger LOG = Logger.get(BugHuntApp.class);

    /** Best score seen since launch (in memory only); seeds each new match's high score. */
    private int sessionBest;
    private MatchSession session;
    private FixedStepClock clock;
    private MatchView view;
    private Hud hud;
    /** Set when the current game cannot go on (failed to start or crashed); shown once, then back to the menu. */
    private String failure;
    /** True once an error has stopped the current game; it no longer updates. */
    private boolean leaving;
    /** True once the current match has reached game over and the trip to the menu is scheduled. */
    private boolean gameOverSeen;

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.startsWith(QA_SHOTS_ARG)) {
                System.setProperty(QA_SHOTS_PROPERTY, arg.substring(QA_SHOTS_ARG.length()));
            }
        }
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(Theme.WIDTH);
        settings.setHeight(Theme.HEIGHT);
        settings.setTitle("Bug Hunt");
        settings.setVersion("1.0");
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
        settings.setFullScreenAllowed(true);
    }

    @Override
    protected void initInput() {
        new GameControls().install(getInput(), () -> session);
    }

    @Override
    protected void onPreInit() {
        // Any letterbox area FXGL leaves around its scaled content gets the game background, not white.
        getPrimaryStage().getScene().setFill(Theme.BACKGROUND);
        String qaDir = System.getProperty(QA_SHOTS_PROPERTY);
        if (qaDir != null && !qaDir.isBlank()) {
            new QaShots(Path.of(qaDir), () -> session).start();
        }
    }

    /** Runs on FXGL's loading thread for every new game. */
    @Override
    protected void initGame() {
        if (session != null) {
            // A match left through the menu still counts towards the session best.
            sessionBest = Math.max(sessionBest, session.match().score().highScore());
        }
        session = null;
        view = null;
        hud = null;
        failure = null;
        leaving = false;
        gameOverSeen = false;
        clock = new FixedStepClock(Ticks.PER_SECOND, MAX_TICKS_PER_FRAME);
        getGameScene().setBackgroundColor(Theme.BACKGROUND);
        try {
            session = new MatchSession(MazeLibrary.fromClasspath(), sessionBest);
        } catch (InvalidMazeException e) {
            LOG.warning("Could not start a match: " + e.getMessage(), e);
            failure = e.getMessage();
        } catch (RuntimeException e) {
            LOG.fatal("Could not start a match", e);
            failure = describe(e);
        }
    }

    /** Runs on FXGL's loading thread, right after {@link #initGame}. */
    @Override
    protected void initUI() {
        if (session == null) {
            return;
        }
        try {
            view = new MatchView(session);
            view.root().setTranslateX(Theme.MAZE_X);
            view.root().setTranslateY(Theme.MAZE_Y);
            hud = new Hud(() -> session);
            hud.root().setTranslateX(Theme.HUD_X);
            hud.root().setTranslateY(Theme.MAZE_Y);
            getGameScene().addUINodes(view.root(), hud.root());
        } catch (RuntimeException e) {
            LOG.fatal("Could not build the match view", e);
            failure = describe(e);
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (leaving) {
            return;
        }
        if (failure != null) {
            leaveWithError("The game could not start", failure);
            return;
        }
        try {
            int ticks = clock.advance(tpf);
            for (int i = 0; i < ticks; i++) {
                session.tick();
            }
            view.update(tpf);
            hud.update();
            if (session.isOver() && !gameOverSeen) {
                onGameOver();
            }
        } catch (RuntimeException e) {
            LOG.fatal("The game stopped because of an unexpected error", e);
            leaveWithError("Unexpected error", describe(e));
        }
    }

    /** Records the best score and leaves the "GAME OVER" banner up for a moment before the menu. */
    private void onGameOver() {
        gameOverSeen = true;
        sessionBest = Math.max(sessionBest, session.match().score().highScore());
        MatchSession finished = session;
        getGameTimer().runOnceAfter(() -> {
            // The player may have left through the game menu and started another game meanwhile.
            if (session == finished) {
                getGameController().gotoMainMenu();
            }
        }, GAME_OVER_HOLD);
    }

    private void leaveWithError(String title, String message) {
        leaving = true;
        ErrorDialog.show(title, message, () -> getGameController().gotoMainMenu());
    }

    private static String describe(RuntimeException e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
