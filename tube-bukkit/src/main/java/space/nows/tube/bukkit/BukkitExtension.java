package space.nows.tube.bukkit;

import space.nows.tube.api.Server;
import space.nows.tube.api.ServerExtension;

import java.nio.file.Path;

public final class BukkitExtension implements ServerExtension {

    private BukkitRuntime runtime;

    @Override
    public String name() {
        return "Bukkit layer";
    }

    @Override
    public void enable(Server server, Path pluginsDirectory) {
        runtime = BukkitRuntime.install(server, pluginsDirectory);
        runtime.loadPlugins();
    }

    @Override
    public void disable() {
        if (runtime != null) {
            runtime.shutdown();
            runtime = null;
        }
    }
}
