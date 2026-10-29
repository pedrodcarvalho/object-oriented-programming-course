package bughunt.qa;

import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getPrimaryStage;
import static com.almasb.fxgl.dsl.FXGL.getSceneService;

import bughunt.model.actors.Debugger;
import bughunt.model.core.Match;
import bughunt.model.core.Ticks;
import bughunt.model.flow.MatchSession;
import bughunt.model.flow.PhaseKind;
import com.almasb.fxgl.scene.Scene;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javafx.animation.AnimationTimer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/**
 * Scripted screenshot run for visual QA, switched on by {@code --qa-shots=<dir>}: it walks
 * from the main menu through a game, the pause menu and a forced game over, saving a PNG of
 * the window at each stop, waits for the game to return to the main menu, then exits with
 * status 0. A failed shot exits with 1; a watchdog exits with 2 if the script has not finished
 * within {@link #WATCHDOG_SECONDS}.
 *
 * <p>Each step waits for a condition on the real game (which scene is showing, the match
 * phase and its tick count), then for a short settle time, so the script follows the game's
 * own clock instead of guessing at frame rates.
 */
public final class QaShots {
    private static final long WATCHDOG_SECONDS = 60;
    private static final int EXIT_SHOT_FAILED = 1;
    private static final int EXIT_TIMED_OUT = 2;
    private static final long EXIT_GRACE_MILLIS = 5000;
    /** Lets FXGL's menu fade-in animations finish before a menu is captured. */
    private static final double MENU_SETTLE = 1.0;
    private static final double NO_SETTLE = 0;

    private final Path dir;
    private final Supplier<MatchSession> session;
    private final List<Step> steps;
    private final AnimationTimer driver = new AnimationTimer() {
        @Override
        public void handle(long now) {
            advance(now);
        }
    };
    /** Read by the watchdog thread to report where the script stalled. */
    private volatile int current;
    /** When the current step's condition first held, in nanoseconds; -1 while it does not hold yet. */
    private long readySince = -1;

    /** {@code session} supplies whatever match is current, or null before the first game. */
    public QaShots(Path dir, Supplier<MatchSession> session) {
        this.dir = dir;
        this.session = session;
        this.steps = List.of(
                new Step("main menu", () -> isShowing(getSceneService().getMainMenuScene().orElse(null)), MENU_SETTLE,
                        () -> shoot("00-main-menu.png")),
                new Step("start a new game", () -> true, NO_SETTLE,
                        () -> getGameController().startNewGame()),
                new Step("1 s into READY", () -> inGame() && phaseAt(PhaseKind.READY, Ticks.of(1)), NO_SETTLE,
                        () -> shoot("01-ready.png")),
                new Step("3 s of play (5 s in)", () -> inGame() && phaseAt(PhaseKind.PLAYING, Ticks.of(3)), NO_SETTLE,
                        () -> shoot("02-gameplay.png")),
                new Step("open the game menu", () -> true, NO_SETTLE,
                        () -> getGameController().gotoGameMenu()),
                new Step("game menu", () -> isShowing(getSceneService().getGameMenuScene().orElse(null)), MENU_SETTLE,
                        () -> shoot("03-pause.png")),
                new Step("resume", () -> true, NO_SETTLE,
                        () -> getSceneService().popSubScene()),
                new Step("back in the game", this::inGame, NO_SETTLE,
                        this::forceGameOver),
                new Step("GAME OVER", () -> inGame() && phaseAt(PhaseKind.GAME_OVER, Ticks.of(0.5)), NO_SETTLE,
                        () -> shoot("04-game-over.png")),
                new Step("back to the main menu after game over",
                        () -> isShowing(getSceneService().getMainMenuScene().orElse(null)), NO_SETTLE,
                        this::succeed));
    }

    /** Starts the watchdog and the script; call on the JavaFX thread before the main menu shows. */
    public void start() {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            fail("cannot create " + dir.toAbsolutePath(), e);
            return;
        }
        log("saving screenshots to " + dir.toAbsolutePath());
        Thread.ofPlatform().daemon().name("qa-watchdog").start(this::watch);
        driver.start();
    }

    private void advance(long now) {
        if (current >= steps.size()) {
            return;
        }
        Step step = steps.get(current);
        if (readySince < 0) {
            if (!step.ready().getAsBoolean()) {
                return;
            }
            readySince = now;
        }
        if ((now - readySince) / 1e9 < step.settleSeconds()) {
            return;
        }
        log("step " + (current + 1) + "/" + steps.size() + ": " + step.name());
        current++;
        readySince = -1;
        step.action().run();
    }

    /** Down to one life, then a catch: the life-lost animation ends in GAME OVER. */
    private void forceGameOver() {
        Match match = session.get().match();
        Debugger debugger = match.debugger();
        while (debugger.lives() > 1) {
            debugger.loseLife();
        }
        match.captureDebugger();
    }

    private void shoot(String fileName) {
        Path file = dir.resolve(fileName);
        try {
            WritableImage image = getPrimaryStage().getScene().snapshot(null);
            if (!ImageIO.write(toBufferedImage(image), "png", file.toFile())) {
                throw new IOException("no PNG writer available");
            }
            log("saved " + file);
        } catch (IOException | RuntimeException e) {
            fail("could not save " + file, e);
        }
    }

    /** JavaFX image to AWT image, pixel by pixel (avoids a dependency on the javafx-swing module). */
    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] argb = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, width, height, argb, 0, width);
        return out;
    }

    private boolean inGame() {
        return session.get() != null && isShowing(getSceneService().getGameScene());
    }

    private boolean phaseAt(PhaseKind kind, int minTicks) {
        MatchSession match = session.get();
        return match.phase().kind() == kind && match.phase().elapsedTicks() >= minTicks;
    }

    private static boolean isShowing(Scene scene) {
        return scene != null && getSceneService().getCurrentScene() == scene;
    }

    private void watch() {
        try {
            Thread.sleep(WATCHDOG_SECONDS * 1000);
        } catch (InterruptedException e) {
            return;
        }
        String at = current < steps.size() ? steps.get(current).name() : "exit";
        System.err.println("[QA] FAILED: watchdog timed out after " + WATCHDOG_SECONDS + " s waiting for: " + at);
        System.err.flush();
        Runtime.getRuntime().halt(EXIT_TIMED_OUT);
    }

    /**
     * Leaves through FXGL's own exit, as the menu's EXIT does, so FXGL saves its system files;
     * the JVM then ends with status 0. Should that shutdown stall, status 0 is forced after a grace period.
     */
    private void succeed() {
        driver.stop();
        log("all shots saved, exiting");
        System.out.flush();
        Thread.ofPlatform().daemon().name("qa-exit-fallback").start(() -> {
            try {
                Thread.sleep(EXIT_GRACE_MILLIS);
            } catch (InterruptedException e) {
                return;
            }
            System.exit(0);
        });
        getGameController().exit();
    }

    /** Exits with {@code EXIT_SHOT_FAILED} off the JavaFX thread, so shutdown hooks that need it cannot deadlock. */
    private void fail(String what, Exception cause) {
        driver.stop();
        System.err.println("[QA] FAILED: " + what + ": " + cause);
        System.err.flush();
        Thread.ofPlatform().name("qa-exit").start(() -> System.exit(EXIT_SHOT_FAILED));
    }

    private static void log(String message) {
        System.out.println("[QA] " + message);
    }

    /** Wait until {@code ready} holds, then {@code settleSeconds} more, then run {@code action}. */
    private record Step(String name, BooleanSupplier ready, double settleSeconds, Runnable action) {
    }
}
