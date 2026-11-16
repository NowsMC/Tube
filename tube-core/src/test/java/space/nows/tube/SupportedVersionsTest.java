package space.nows.tube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

class SupportedVersionsTest {

    @Test
    void listsConfiguredMinecraftVersions() {
        assertEquals(List.of("26.1.2", "26.2", "26.3"), SupportedVersions.minecraft());
    }

    @Test
    void findsOnlyKnownVersions() {
        assertTrue(SupportedVersions.find("26.2").isPresent());
        assertTrue(SupportedVersions.find("1.0").isEmpty());
    }

    @Test
    void buildsLaunchCommand() {
        LaunchSpec spec = new LaunchSpec(Path.of("java"), Path.of("/srv/server.jar"), Path.of("/srv"), List.of("-Xmx2G"));
        assertEquals(List.of("java", "-Xmx2G", "-jar", "/srv/server.jar", "nogui"), spec.command());
    }
}
