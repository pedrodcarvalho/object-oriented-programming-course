package bugcatcher;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getGameTimer;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;
import static com.almasb.fxgl.dsl.FXGL.onKey;

import bugcatcher.items.FallingItem;
import bugcatcher.items.ItemFactory;
import bugcatcher.player.HighScores;
import bugcatcher.player.Player;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.time.TimerAction;
import java.nio.file.Path;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class BugCatcherApp extends GameApplication {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;

    private static final double BASKET_WIDTH = 110;
    private static final double BASKET_HEIGHT = 22;
    private static final double BASKET_STEP = 8;

    private static final Duration SPAWN_EVERY = Duration.seconds(0.7);
    private static final Color BACKGROUND = Color.web("#1e1e2e");

    public static final Path SCORES_FILE = Path.of("highscores.txt");

    private static String playerName = "Player";

    private Player player;
    private HighScores highScores;
    private Entity basket;
    private Text hudText;
    private TimerAction spawner;
    private boolean gameOver;

    public static void main(String[] args) {
        launch(args);
    }

    public static void setPlayerName(String name) {
        playerName = name;
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(WIDTH);
        settings.setHeight(HEIGHT);
        settings.setTitle("Bug Catcher");
        settings.setVersion("1.1");

        settings.setMainMenuEnabled(true);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new NameMenu();
            }
        });
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.LEFT, () -> moveBasket(-BASKET_STEP));
        onKey(KeyCode.RIGHT, () -> moveBasket(BASKET_STEP));
        onKey(KeyCode.A, () -> moveBasket(-BASKET_STEP));
        onKey(KeyCode.D, () -> moveBasket(BASKET_STEP));
    }

    @Override
    protected void initGame() {
        player = new Player(playerName);
        highScores = new HighScores(SCORES_FILE);
        gameOver = false;

        getGameScene().setBackgroundColor(BACKGROUND);

        basket = entityBuilder()
                .type(EntityType.BASKET)
                .at((WIDTH - BASKET_WIDTH) / 2, HEIGHT - 70)
                .viewWithBBox(new Rectangle(BASKET_WIDTH, BASKET_HEIGHT, Color.LIGHTSKYBLUE))
                .collidable()
                .buildAndAttach();

        spawner = getGameTimer().runAtInterval(this::spawnItem, SPAWN_EVERY);
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BASKET, EntityType.ITEM) {
            @Override
            protected void onCollisionBegin(Entity basket, Entity itemEntity) {
                if (gameOver) {
                    return;
                }
                FallingItem item = itemEntity.getObject("item");
                itemEntity.removeFromWorld();
                player.caught(item);

                if (!player.isAlive()) {
                    endGame();
                }
            }
        });
    }

    @Override
    protected void initUI() {
        hudText = new Text();
        hudText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        hudText.setFill(Color.WHITE);
        hudText.setTranslateX(20);
        hudText.setTranslateY(35);

        Text help = new Text("Arrows or A/D to move. Catch coffee, dodge the BUGs!");
        help.setFont(Font.font("Arial", 14));
        help.setFill(Color.GRAY);
        help.setTranslateX(20);
        help.setTranslateY(HEIGHT - 20);

        getGameScene().addUINodes(hudText, help);
        updateHud();
    }

    @Override
    protected void onUpdate(double tpf) {
        if (gameOver) {
            return;
        }
        for (Entity entity : getGameWorld().getEntitiesByType(EntityType.ITEM)) {
            FallingItem item = entity.getObject("item");
            entity.translateY(item.getSpeed() * tpf);

            if (entity.getY() > HEIGHT) {
                entity.removeFromWorld();
            }
        }
        updateHud();
    }

    private void spawnItem() {
        FallingItem item = ItemFactory.randomItem(player.getLevel());
        double x = Math.random() * (WIDTH - FallingItem.SIZE);

        entityBuilder()
                .type(EntityType.ITEM)
                .at(x, -FallingItem.SIZE)
                .viewWithBBox(item.createShape())
                .view(item.createLabel())
                .with("item", item)
                .collidable()
                .buildAndAttach();
    }

    private void moveBasket(double dx) {
        if (gameOver) {
            return;
        }
        double x = basket.getX() + dx;
        x = Math.max(0, Math.min(WIDTH - BASKET_WIDTH, x));
        basket.setX(x);
    }

    private void updateHud() {
        hudText.setText(String.format("Score: %d    Lives: %d    Level: %d    Best: %d",
                player.getScore(), player.getLives(), player.getLevel(),
                Math.max(highScores.getBest(), player.getScore())));
    }

    private void endGame() {
        gameOver = true;
        spawner.expire();
        updateHud();

        int score = player.getScore();
        boolean newRecord = score > highScores.getBest();
        highScores.add(player.getName(), score);
        highScores.save();

        String title = newRecord ? "NEW RECORD!" : "GAME OVER";
        String message = String.format("%s%n%n%s, you scored %d points.%nYou caught %s.%n%nTop %d:%n%s",
                title, player.getName(), score, player.getSummary(), HighScores.SIZE, highScores);

        getDialogService().showMessageBox(message, () -> getGameController().gotoMainMenu());
    }
}
