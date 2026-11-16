package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.List;

class FileConfigTest {

    @TempDir
    Path directory;

    @Test
    void readsBackSavedNestedValues() {
        Path file = directory.resolve("config.yml");
        FileConfig config = FileConfig.load(file);
        config.set("spawn.x", 1.5);
        config.set("name", "tube");
        config.set("enabled", true);
        config.set("tags", List.of("a", "b"));
        config.save();

        FileConfig reloaded = FileConfig.load(file);
        assertEquals(1.5, reloaded.getDouble("spawn.x", 0));
        assertEquals("tube", reloaded.getString("name", ""));
        assertTrue(reloaded.getBoolean("enabled", false));
        assertEquals(List.of("a", "b"), reloaded.getStringList("tags"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"config.yml", "config.json", "config.toml"})
    void roundTripsEveryFormat(String fileName) {
        Path file = directory.resolve(fileName);
        FileConfig config = FileConfig.load(file);
        config.set("server.port", 25565);
        config.set("server.name", "tube");
        config.set("ratio", 0.25);
        config.set("enabled", true);
        config.set("tags", List.of("a", "b"));
        config.save();

        FileConfig reloaded = FileConfig.load(file);
        assertEquals(25565, reloaded.getInt("server.port", 0));
        assertEquals("tube", reloaded.getString("server.name", ""));
        assertEquals(0.25, reloaded.getDouble("ratio", 0));
        assertTrue(reloaded.getBoolean("enabled", false));
        assertEquals(List.of("a", "b"), reloaded.getStringList("tags"));
    }

    @Test
    void rejectsUnknownExtension() {
        assertThrows(IllegalArgumentException.class, () -> FileConfig.load(directory.resolve("config.ini")));
    }

    @Test
    void returnsFallbackForMissingOrMistypedValues() {
        FileConfig config = FileConfig.load(directory.resolve("missing.yml"));
        config.set("name", "tube");
        assertFalse(config.contains("other"));
        assertEquals(7, config.getInt("other", 7));
        assertEquals(3, config.getInt("name", 3));
    }
}
