package space.nows.tube.server.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class NetworkSettingsTest {

    @TempDir
    Path directory;

    @Test
    void remoteAccessIsOffByDefault() throws IOException {
        Path file = directory.resolve("server.properties");

        ServerSettings settings = ServerProperties.load(file);

        assertFalse(settings.rcon().enabled());
        assertFalse(settings.query().enabled());
        assertEquals(25575, settings.rcon().port());
        assertEquals(25565, settings.query().port());
        assertEquals(256, settings.connection().compressionThreshold());
        assertTrue(Files.readString(file).contains("rcon.password="));
    }

    @Test
    void readsRconQueryAndResourcePackKeys() throws IOException {
        Path file = directory.resolve("server.properties");
        Files.writeString(file, "enable-rcon=true\nrcon.port=30001\nrcon.password=secret\nenable-query=true\nquery.port=30002\n"
                + "network-compression-threshold=-1\nresource-pack=https://example.com/pack.zip\nrequire-resource-pack=true\n"
                + "resource-pack-id=123e4567-e89b-12d3-a456-426614174000\n");

        ServerSettings settings = ServerProperties.load(file);

        assertTrue(settings.rcon().enabled());
        assertEquals(30001, settings.rcon().port());
        assertEquals("secret", settings.rcon().password());
        assertTrue(settings.query().enabled());
        assertEquals(30002, settings.query().port());
        assertEquals(-1, settings.connection().compressionThreshold());
        assertEquals("https://example.com/pack.zip", settings.resourcePack().url());
        assertTrue(settings.resourcePack().required());
        assertEquals("123e4567-e89b-12d3-a456-426614174000", settings.resourcePack().id());
    }

    @Test
    void ignoresAMalformedResourcePackId() throws IOException {
        Path file = directory.resolve("server.properties");
        Files.writeString(file, "resource-pack-id=not-a-uuid\n");

        assertEquals("", ServerProperties.load(file).resourcePack().id());
    }
}
