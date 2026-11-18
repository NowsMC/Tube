package space.nows.tube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

class SupportedVersionsTest {

    @Test
    void listsConfiguredMinecraftVersions() {
        assertEquals(List.of("26.2"), SupportedVersions.minecraft());
    }

    @Test
    void everyEnabledVersionIsBuilt() throws java.io.IOException {
        String modules = java.nio.file.Files.readString(Path.of("..", "versions", "pom.xml"));
        for (String version : SupportedVersions.minecraft()) {
            assertTrue(modules.contains("<module>" + version + "</module>"), version + " is enabled but not built in versions/pom.xml");
        }
        java.util.regex.Matcher built = java.util.regex.Pattern.compile("<module>([^<]+)</module>").matcher(modules);
        while (built.find()) {
            assertTrue(SupportedVersions.minecraft().contains(built.group(1)), built.group(1) + " is built but not enabled in tube.minecraft.versions");
        }
    }

    @Test
    void findsOnlyKnownVersions() {
        assertTrue(SupportedVersions.find("26.2").isPresent());
        assertTrue(SupportedVersions.find("1.0").isEmpty());
    }

    @Test
    void buildsLaunchCommand() {
        LaunchSpec spec = new LaunchSpec(Path.of("java"), Path.of("server.jar"), Path.of("srv"), List.of("-Xmx2G"));
        assertEquals(List.of("java", "-D" + LaunchSpec.RESTART_PROPERTY + "=" + LaunchSpec.RESTART_EXIT_CODE, "-Xmx2G", "-jar", Path.of("server.jar").toAbsolutePath().toString(), "nogui"), spec.command());
    }
}
