package space.nows.tube.server;

import space.nows.tube.api.PluginConfig;

import java.nio.file.Path;

public record ServerConfig(String host, int port, boolean onlineMode, String defaultWorld, int autosaveSeconds) {

    public static ServerConfig load(Path file) {
        YamlConfig config = YamlConfig.load(file);
        boolean changed = false;
        changed |= defaultTo(config, "host", "0.0.0.0");
        changed |= defaultTo(config, "port", 25565);
        changed |= defaultTo(config, "online-mode", true);
        changed |= defaultTo(config, "default-world", "world");
        changed |= defaultTo(config, "autosave-seconds", 300);
        if (changed) {
            config.save();
        }
        return new ServerConfig(
                config.getString("host", "0.0.0.0"),
                config.getInt("port", 25565),
                config.getBoolean("online-mode", true),
                config.getString("default-world", "world"),
                config.getInt("autosave-seconds", 300));
    }

    private static boolean defaultTo(PluginConfig config, String path, Object value) {
        if (config.contains(path)) {
            return false;
        }
        config.set(path, value);
        return true;
    }
}
