package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class NetworkSettingsTest {

    @TempDir
    Path directory;

    @Test
    void remoteAccessIsOffByDefault() throws IOException {
        Path file = directory.resolve("server.properties");

        ServerSettings settings = ServerSettings.load(file);

        assertFalse(settings.enableRcon());
        assertFalse(settings.enableQuery());
        assertEquals(25575, settings.rconPort());
        assertEquals(25565, settings.queryPort());
        assertEquals(256, settings.networkCompressionThreshold());
        assertTrue(Files.readString(file).contains("rcon.password="));
    }

    @Test
    void readsRconQueryAndResourcePackKeys() throws IOException {
        Path file = directory.resolve("server.properties");
        Files.writeString(file, "enable-rcon=true\nrcon.port=30001\nrcon.password=secret\nenable-query=true\nquery.port=30002\n"
                + "network-compression-threshold=-1\nresource-pack=https://example.com/pack.zip\nrequire-resource-pack=true\n"
                + "resource-pack-id=123e4567-e89b-12d3-a456-426614174000\n");

        ServerSettings settings = ServerSettings.load(file);

        assertTrue(settings.enableRcon());
        assertEquals(30001, settings.rconPort());
        assertEquals("secret", settings.rconPassword());
        assertTrue(settings.enableQuery());
        assertEquals(30002, settings.queryPort());
        assertEquals(-1, settings.networkCompressionThreshold());
        assertEquals("https://example.com/pack.zip", settings.resourcePack());
        assertTrue(settings.requireResourcePack());
        assertEquals("123e4567-e89b-12d3-a456-426614174000", settings.resourcePackId());
    }

    @Test
    void ignoresAMalformedResourcePackId() throws IOException {
        Path file = directory.resolve("server.properties");
        Files.writeString(file, "resource-pack-id=not-a-uuid\n");

        assertEquals("", ServerSettings.load(file).resourcePackId());
    }

    @Test
    void readsTheClientBrandFromAPluginMessage() {
        byte[] message = {7, 'v', 'a', 'n', 'i', 'l', 'l', 'a'};
        assertEquals("vanilla", TubePluginChannels.readBrand(message));
        assertEquals("", TubePluginChannels.readBrand(new byte[0]));
        assertEquals("fabric", TubePluginChannels.readBrand(("\u0006fabric").getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void channelNamesDefaultToTheMinecraftNamespace() {
        assertEquals("minecraft:brand", TubePluginChannels.normalize("Brand"));
        assertEquals("bungeecord:main", TubePluginChannels.normalize("BungeeCord:Main"));
    }
}
