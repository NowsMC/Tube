package space.nows.tube.bootstrap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

class BootstrapTest {

    @TempDir
    Path directory;

    private void jar(String name, String entry) throws IOException {
        try (OutputStream out = Files.newOutputStream(directory.resolve(name)); JarOutputStream jar = new JarOutputStream(out)) {
            jar.putNextEntry(new JarEntry(entry));
            jar.write(1);
            jar.closeEntry();
        }
    }

    @Test
    void detectsPluginsThatNeedTheBukkitLibraries() throws IOException {
        jar("tube-plugin.jar", "tube-plugin.yml");
        assertFalse(Bootstrap.hasBukkitPlugins(directory));

        jar("bukkit-plugin.jar", "plugin.yml");
        assertTrue(Bootstrap.hasBukkitPlugins(directory));
    }

    @Test
    void aMissingPluginsFolderMeansNoBukkitPlugins() throws IOException {
        assertFalse(Bootstrap.hasBukkitPlugins(directory.resolve("plugins")));
    }
}
