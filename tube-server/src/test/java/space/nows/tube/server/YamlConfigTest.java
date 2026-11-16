package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

class YamlConfigTest {

    @TempDir
    Path directory;

    @Test
    void readsBackSavedNestedValues() {
        Path file = directory.resolve("config.yml");
        YamlConfig config = YamlConfig.load(file);
        config.set("spawn.x", 1.5);
        config.set("name", "tube");
        config.set("enabled", true);
        config.set("tags", List.of("a", "b"));
        config.save();

        YamlConfig reloaded = YamlConfig.load(file);
        assertEquals(1.5, reloaded.getDouble("spawn.x", 0));
        assertEquals("tube", reloaded.getString("name", ""));
        assertTrue(reloaded.getBoolean("enabled", false));
        assertEquals(List.of("a", "b"), reloaded.getStringList("tags"));
    }

    @Test
    void returnsFallbackForMissingOrMistypedValues() {
        YamlConfig config = YamlConfig.load(directory.resolve("missing.yml"));
        config.set("name", "tube");
        assertFalse(config.contains("other"));
        assertEquals(7, config.getInt("other", 7));
        assertEquals(3, config.getInt("name", 3));
    }

    @Test
    void serverConfigWritesDefaultsOnFirstLoad() {
        Path file = directory.resolve("tube.yml");
        ServerConfig config = ServerConfig.load(file);
        assertEquals(25565, config.port());
        assertTrue(config.onlineMode());
        assertTrue(java.nio.file.Files.exists(file));
        assertEquals(config, ServerConfig.load(file));
    }
}
