package bughunt;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Keeps the model engine-free: nothing under bughunt.model may import JavaFX or FXGL. */
class ArchitectureTest {
    private static final Path MODEL_SOURCES = Path.of("src/main/java/bughunt/model");

    @Test
    void modelHasNoEngineImports() throws IOException {
        if (!Files.isDirectory(MODEL_SOURCES)) {
            return;
        }
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(MODEL_SOURCES)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                for (String line : Files.readAllLines(file)) {
                    String trimmed = line.strip();
                    if (trimmed.startsWith("import javafx") || trimmed.startsWith("import com.almasb")) {
                        offenders.add(file + ": " + trimmed);
                    }
                }
            }
        }
        assertTrue(offenders.isEmpty(), "Model must not import engine classes: " + offenders);
    }
}
