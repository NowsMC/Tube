package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.GameMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ServerSettingsTest {

    @TempDir
    Path directory;

    @Test
    void writesVanillaStyleDefaultsOnFirstLoad() throws IOException {
        Path file = directory.resolve("server.properties");

        ServerSettings settings = ServerSettings.load(file);

        assertEquals(25565, settings.serverPort());
        assertTrue(settings.onlineMode());
        assertEquals("0.0.0.0", settings.bindAddress());
        assertEquals("world", settings.levelName());
        assertEquals(GameMode.SURVIVAL, settings.gamemode());
        assertEquals(20, settings.maxPlayers());
        String content = Files.readString(file);
        assertTrue(content.contains("server-port=25565"));
        assertTrue(content.contains("motd=A Tube Server"));
    }

    @Test
    void keepsExistingValuesAndAppendsMissingKeys() throws IOException {
        Path file = directory.resolve("server.properties");
        Files.writeString(file, "server-port=30000\nonline-mode=false\ngamemode=1\ndifficulty=hard\nmy-plugin-key=kept\n");

        ServerSettings settings = ServerSettings.load(file);

        assertEquals(30000, settings.serverPort());
        assertFalse(settings.onlineMode());
        assertEquals(GameMode.CREATIVE, settings.gamemode());
        assertEquals(ServerSettings.Difficulty.HARD, settings.difficulty());
        String content = Files.readString(file);
        assertTrue(content.contains("my-plugin-key=kept"));
        assertTrue(content.contains("max-players=20"));
        assertEquals(1, content.split("server-port", -1).length - 1);
    }

    @Test
    void fallsBackToDefaultsForInvalidValues() throws IOException {
        Path file = directory.resolve("server.properties");
        Files.writeString(file, "server-port=99999\nonline-mode=maybe\nlevel-name=../escape\ngamemode=weird\nmax-players=0\n");

        ServerSettings settings = ServerSettings.load(file);

        assertEquals(25565, settings.serverPort());
        assertTrue(settings.onlineMode());
        assertEquals("world", settings.levelName());
        assertEquals(GameMode.SURVIVAL, settings.gamemode());
        assertEquals(20, settings.maxPlayers());
    }
}
