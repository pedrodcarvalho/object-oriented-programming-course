package bughunt;

import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getSettings;

import bughunt.model.core.Direction;
import bughunt.model.flow.MatchSession;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import java.util.function.Supplier;
import javafx.beans.property.BooleanProperty;
import javafx.scene.input.KeyCode;

/**
 * The fixed key bindings: WASD and the arrow keys steer, P opens the game menu (Esc already
 * does) and F11 toggles fullscreen. FXGL wants one action per key, so each key gets its own.
 */
final class GameControls {

    /** Binds every key; steering goes to whatever session is current when the key is pressed. */
    void install(Input input, Supplier<MatchSession> session) {
        steer(input, session, "Up (W)", KeyCode.W, Direction.UP);
        steer(input, session, "Up (arrow)", KeyCode.UP, Direction.UP);
        steer(input, session, "Left (A)", KeyCode.A, Direction.LEFT);
        steer(input, session, "Left (arrow)", KeyCode.LEFT, Direction.LEFT);
        steer(input, session, "Down (S)", KeyCode.S, Direction.DOWN);
        steer(input, session, "Down (arrow)", KeyCode.DOWN, Direction.DOWN);
        steer(input, session, "Right (D)", KeyCode.D, Direction.RIGHT);
        steer(input, session, "Right (arrow)", KeyCode.RIGHT, Direction.RIGHT);
        input.addAction(onPress("Pause (P)", () -> getGameController().gotoGameMenu()), KeyCode.P);
        input.addAction(onPress("Fullscreen (F11)", GameControls::toggleFullScreen), KeyCode.F11);
    }

    private static void steer(Input input, Supplier<MatchSession> session, String name, KeyCode key,
                              Direction direction) {
        input.addAction(onPress(name, () -> {
            MatchSession current = session.get();
            if (current != null) {
                current.requestDirection(direction);
            }
        }), key);
    }

    private static void toggleFullScreen() {
        BooleanProperty fullScreen = getSettings().getFullScreen();
        fullScreen.set(!fullScreen.get());
    }

    /** An action that does its work once, when the key goes down. */
    private static UserAction onPress(String name, Runnable work) {
        return new UserAction(name) {
            @Override
            protected void onActionBegin() {
                work.run();
            }
        };
    }
}
