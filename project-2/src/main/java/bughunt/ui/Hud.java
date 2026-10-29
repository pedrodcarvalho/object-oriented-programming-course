package bughunt.ui;

import bughunt.model.core.Match;
import bughunt.model.flow.MatchSession;
import bughunt.model.rules.ModeController;
import bughunt.view.Theme;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * The panel to the right of the maze: score, high score, level, lives, the bugs' current
 * wave, the bits left and a one-line controls hint, as neon labels. It reads the session
 * every frame and only touches a label when its value changed.
 */
public final class Hud {
    private static final double CAPTION_SIZE = 14;
    private static final double VALUE_SIZE = 28;
    private static final double HINT_SIZE = 12;
    private static final double ROW_GAP = 4;
    private static final double SECTION_GAP = 22;
    private static final String HINT = "WASD/Arrows move · P pause · F11 fullscreen";

    private final Supplier<MatchSession> session;
    private final VBox root = new VBox(ROW_GAP);
    private final Text score = value(Theme.BIT);
    private final Text highScore = value(Theme.DEBUGGER);
    private final Text level = value(Theme.BIT);
    private final Text lives = value(Theme.DEBUGGER);
    private final Text wave = value(Theme.TRACE);
    private final Text bits = value(Theme.BIT);

    public Hud(Supplier<MatchSession> session) {
        this.session = session;
        root.setPrefWidth(Theme.HUD_WIDTH);
        root.setMaxWidth(Theme.HUD_WIDTH);
        section("SCORE", score);
        section("HIGH SCORE", highScore);
        section("LEVEL", level);
        section("LIVES", lives);
        section("WAVE", wave);
        section("BITS LEFT", bits);
        Text hint = new Text(HINT);
        hint.setFont(Theme.body(HINT_SIZE));
        hint.setFill(Theme.TRACE);
        hint.setWrappingWidth(Theme.HUD_WIDTH);
        VBox.setMargin(hint, new Insets(SECTION_GAP, 0, 0, 0));
        root.getChildren().add(hint);
        update();
    }

    /** The panel's node; place it at {@code Theme.HUD_X}. */
    public Region root() {
        return root;
    }

    /** Called every frame. */
    public void update() {
        MatchSession current = session.get();
        if (current == null) {
            return;
        }
        Match match = current.match();
        ModeController modes = match.modes();
        set(score, Integer.toString(match.score().score()));
        set(highScore, Integer.toString(match.score().highScore()));
        set(level, Integer.toString(match.levelNumber()));
        set(lives, Integer.toString(match.debugger().lives()));
        if (modes.isBreakpointActive()) {
            set(wave, "BREAKPOINT");
            wave.setFill(Theme.VULNERABLE);
        } else {
            set(wave, modes.waveName());
            wave.setFill(Theme.TRACE);
        }
        set(bits, Integer.toString(match.pickupsRemaining()));
    }

    private void section(String caption, Text value) {
        Text label = new Text(caption);
        label.setFont(Theme.body(CAPTION_SIZE));
        label.setFill(Theme.TRACE);
        if (!root.getChildren().isEmpty()) {
            VBox.setMargin(label, new Insets(SECTION_GAP - ROW_GAP, 0, 0, 0));
        }
        root.getChildren().addAll(label, value);
    }

    private static Text value(Color colour) {
        Text text = new Text();
        text.setFont(Theme.title(VALUE_SIZE));
        text.setFill(colour);
        return text;
    }

    private static void set(Text text, String value) {
        if (!value.equals(text.getText())) {
            text.setText(value);
        }
    }
}
