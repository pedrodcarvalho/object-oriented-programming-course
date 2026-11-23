package bugcatcher;

import bugcatcher.player.HighScores;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class NameMenu extends FXGLMenu {
    private static final int MAX_NAME_LENGTH = 12;

    private final TextField nameField = new TextField();
    private final Text scoreboard = new Text();
    private final Label error = new Label();

    public NameMenu() {
        super(MenuType.MAIN_MENU);

        Rectangle background = new Rectangle(getAppWidth(), getAppHeight(), Color.web("#1e1e2e"));

        Text title = new Text("BUG CATCHER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setFill(Color.LIGHTSKYBLUE);

        Text ask = new Text("What is your name?");
        ask.setFont(Font.font("Arial", 20));
        ask.setFill(Color.WHITE);

        nameField.setPromptText("Your name");
        nameField.setMaxWidth(240);
        nameField.setFont(Font.font("Arial", 18));
        nameField.setOnAction(event -> play());

        error.setTextFill(Color.SALMON);

        Button playButton = new Button("PLAY");
        playButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        playButton.setOnAction(event -> play());

        Text scoreboardTitle = new Text("TOP " + HighScores.SIZE);
        scoreboardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        scoreboardTitle.setFill(Color.GOLD);

        scoreboard.setFont(Font.font("Monospaced", 18));
        scoreboard.setFill(Color.WHITE);

        VBox box = new VBox(16, title, ask, nameField, error, playButton, scoreboardTitle, scoreboard);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(getAppWidth(), getAppHeight());

        getContentRoot().getChildren().addAll(background, box);
    }

    @Override
    public void onCreate() {
        scoreboard.setText(new HighScores(BugCatcherApp.SCORES_FILE).toString());
        error.setText("");
        nameField.requestFocus();
    }

    private void play() {
        String name = nameField.getText().replace(";", "").trim();
        if (name.isEmpty()) {
            error.setText("Please type a name first.");
            return;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
        }
        BugCatcherApp.setPlayerName(name);
        fireNewGame();
    }
}
