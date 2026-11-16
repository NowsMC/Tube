package space.nows.tube.bukkit;

import space.nows.tube.api.Server;
import space.nows.tube.api.ServerExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.jar.JarFile;

public final class BukkitExtension implements ServerExtension {

    private Object runtime;

    @Override
    public String name() {
        return "Bukkit layer";
    }

    @Override
    public void enable(Server server, Path pluginsDirectory) {
        if (!containsBukkitPlugins(pluginsDirectory.toFile())) {
            return;
        }
        BukkitRuntime started = BukkitRuntime.install(server, pluginsDirectory);
        runtime = started;
        started.loadPlugins();
    }

    @Override
    public void disable() {
        if (runtime instanceof BukkitRuntime started) {
            started.shutdown();
            runtime = null;
        }
    }

    private static boolean containsBukkitPlugins(File directory) {
        File[] jars = directory.listFiles(file -> file.getName().endsWith(".jar"));
        if (jars == null) {
            return false;
        }
        for (File jar : jars) {
            try (JarFile file = new JarFile(jar)) {
                if (file.getEntry("plugin.yml") != null) {
                    return true;
                }
            } catch (java.io.IOException ignored) {
                // a broken jar is not a Bukkit plugin
            }
        }
        return false;
    }
}
