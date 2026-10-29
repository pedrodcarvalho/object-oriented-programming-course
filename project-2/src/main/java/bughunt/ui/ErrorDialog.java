package bughunt.ui;

import static com.almasb.fxgl.dsl.FXGL.getDialogService;

import javafx.application.Platform;

/** Tells the player something went wrong, in FXGL's own message box, and runs {@code onClose} once it is dismissed. */
public final class ErrorDialog {

    private ErrorDialog() {
    }

    /** Safe to call from any thread: the box is always opened on the JavaFX thread. */
    public static void show(String title, String message, Runnable onClose) {
        Runnable open = () -> getDialogService().showMessageBox(title + "\n\n" + message, onClose);
        if (Platform.isFxApplicationThread()) {
            open.run();
        } else {
            Platform.runLater(open);
        }
    }
}
