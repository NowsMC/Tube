package space.nows.tube.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.entity.GameMode;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public final class ServerProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private static final Map<String, String> DEFAULTS = defaults();

    private static final Map<String, String> UNSUPPORTED = unsupported();

    private final Properties properties;

    private ServerProperties(Properties properties) {
        this.properties = properties;
    }

    private static Map<String, String> defaults() {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("server-ip", "");
        defaults.put("server-port", "25565");
        defaults.put("online-mode", "true");
        defaults.put("motd", "A Tube Server");
        defaults.put("max-players", "20");
        defaults.put("level-name", "world");
        defaults.put("level-type", "minecraft:flat");
        defaults.put("gamemode", "survival");
        defaults.put("difficulty", "easy");
        defaults.put("hardcore", "false");
        defaults.put("force-gamemode", "false");
        defaults.put("white-list", "false");
        defaults.put("enforce-whitelist", "false");
        defaults.put("enable-status", "true");
        defaults.put("hide-online-players", "false");
        defaults.put("spawn-protection", "16");
        defaults.put("view-distance", "10");
        defaults.put("log-ips", "true");
        defaults.put("autosave-seconds", "300");
        defaults.put("enable-rcon", "false");
        defaults.put("rcon.port", "25575");
        defaults.put("rcon.password", "");
        defaults.put("broadcast-rcon-to-ops", "true");
        defaults.put("enable-query", "false");
        defaults.put("query.port", "25565");
        defaults.put("network-compression-threshold", "256");
        defaults.put("resource-pack", "");
        defaults.put("resource-pack-sha1", "");
        defaults.put("resource-pack-id", "");
        defaults.put("resource-pack-prompt", "");
        defaults.put("require-resource-pack", "false");
        defaults.put("rate-limit", "0");
        defaults.put("use-native-transport", "true");
        defaults.put("prevent-proxy-connections", "false");
        defaults.put("accepts-transfers", "false");
        defaults.put("player-idle-timeout", "0");
        defaults.put("max-tick-time", "60000");
        return defaults;
    }

    private static Map<String, String> unsupported() {
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put("allow-flight", "false");
        keys.put("enforce-secure-profile", "true");
        keys.put("entity-broadcast-range-percentage", "100");
        keys.put("generate-structures", "true");
        keys.put("generator-settings", "{}");
        keys.put("initial-enabled-packs", "vanilla");
        keys.put("level-seed", "");
        keys.put("max-world-size", "29999984");
        keys.put("pause-when-empty-seconds", "-1");
        keys.put("simulation-distance", "10");
        return keys;
    }

    public static ServerSettings load(Path file) {
        try {
            Properties properties = new Properties();
            if (Files.isRegularFile(file)) {
                properties.load(new StringReader(Files.readString(file, StandardCharsets.UTF_8)));
            }
            complete(file, properties);
            return parse(properties);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static ServerSettings parse(Properties properties) {
        warnUnsupported(properties);
        return new ServerProperties(properties).settings();
    }

    public static void set(Path file, String key, String value) {
        try {
            List<String> lines = Files.isRegularFile(file) ? new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8)) : new ArrayList<>();
            boolean replaced = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.startsWith("#") && line.startsWith(key + "=")) {
                    lines.set(i, key + "=" + value);
                    replaced = true;
                }
            }
            if (!replaced) {
                lines.add(key + "=" + value);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ServerSettings settings() {
        return new ServerSettings(
                new ServerSettings.Connection(
                        text("server-ip"),
                        integer("server-port", 1, 65535),
                        bool("online-mode"),
                        integer("max-players", 1, Integer.MAX_VALUE),
                        bool("log-ips"),
                        bool("prevent-proxy-connections"),
                        bool("accepts-transfers")),
                new ServerSettings.Network(
                        integer("network-compression-threshold", -1, Integer.MAX_VALUE),
                        integer("rate-limit", 0, Integer.MAX_VALUE),
                        bool("use-native-transport")),
                new ServerSettings.Status(
                        text("motd"),
                        bool("enable-status"),
                        bool("hide-online-players")),
                new ServerSettings.Level(
                        levelName(),
                        text("level-type"),
                        integer("view-distance", 2, 32),
                        integer("spawn-protection", 0, Integer.MAX_VALUE),
                        integer("autosave-seconds", 0, Integer.MAX_VALUE)),
                new ServerSettings.Gameplay(
                        gameMode(),
                        bool("force-gamemode"),
                        difficulty(),
                        bool("hardcore"),
                        integer("player-idle-timeout", 0, Integer.MAX_VALUE)),
                new ServerSettings.Access(
                        bool("white-list"),
                        bool("enforce-whitelist")),
                new ServerSettings.Rcon(
                        bool("enable-rcon"),
                        integer("rcon.port", 1, 65535),
                        text("rcon.password"),
                        bool("broadcast-rcon-to-ops")),
                new ServerSettings.Query(
                        bool("enable-query"),
                        integer("query.port", 1, 65535)),
                new ServerSettings.ResourcePack(
                        text("resource-pack"),
                        text("resource-pack-sha1"),
                        resourcePackId(),
                        text("resource-pack-prompt"),
                        bool("require-resource-pack")),
                new ServerSettings.Watchdog(
                        integer("max-tick-time", -1, Integer.MAX_VALUE)));
    }

    private static void complete(Path file, Properties properties) throws IOException {
        boolean created = !Files.isRegularFile(file);
        StringBuilder missing = new StringBuilder();
        String lineBreak = System.lineSeparator();
        for (Map.Entry<String, String> entry : DEFAULTS.entrySet()) {
            if (!properties.containsKey(entry.getKey())) {
                properties.setProperty(entry.getKey(), entry.getValue());
                missing.append(entry.getKey()).append('=').append(entry.getValue()).append(lineBreak);
            }
        }
        if (missing.isEmpty()) {
            return;
        }
        String content = created ? "#Tube server properties" + lineBreak + missing : missing.toString();
        Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static void warnUnsupported(Properties properties) {
        UNSUPPORTED.forEach((key, fallback) -> {
            String value = properties.getProperty(key);
            if (value != null && !value.strip().equals(fallback)) {
                LOGGER.warn("server.properties: {} is not supported by Tube yet and is ignored", key);
            }
        });
    }

    private String text(String key) {
        return properties.getProperty(key, DEFAULTS.get(key)).strip();
    }

    private boolean bool(String key) {
        String value = text(key);
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        return invalid(key, value, Boolean.parseBoolean(DEFAULTS.get(key)));
    }

    private int integer(String key, int min, int max) {
        String value = text(key);
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= min && parsed <= max) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // falls through to the default below
        }
        return invalid(key, value, Integer.parseInt(DEFAULTS.get(key)));
    }

    private String resourcePackId() {
        String value = text("resource-pack-id");
        if (value.isEmpty()) {
            return value;
        }
        try {
            UUID.fromString(value);
            return value;
        } catch (IllegalArgumentException e) {
            return invalid("resource-pack-id", value, "");
        }
    }

    private String levelName() {
        String value = text("level-name");
        return value.matches("[A-Za-z0-9_-]+") ? value : invalid("level-name", value, DEFAULTS.get("level-name"));
    }

    private GameMode gameMode() {
        String value = text("gamemode").toLowerCase(Locale.ROOT);
        return switch (value) {
            case "survival", "0" -> GameMode.SURVIVAL;
            case "creative", "1" -> GameMode.CREATIVE;
            case "adventure", "2" -> GameMode.ADVENTURE;
            case "spectator", "3" -> GameMode.SPECTATOR;
            default -> invalid("gamemode", value, GameMode.SURVIVAL);
        };
    }

    private ServerSettings.Difficulty difficulty() {
        String value = text("difficulty").toLowerCase(Locale.ROOT);
        return switch (value) {
            case "peaceful", "0" -> ServerSettings.Difficulty.PEACEFUL;
            case "easy", "1" -> ServerSettings.Difficulty.EASY;
            case "normal", "2" -> ServerSettings.Difficulty.NORMAL;
            case "hard", "3" -> ServerSettings.Difficulty.HARD;
            default -> invalid("difficulty", value, ServerSettings.Difficulty.EASY);
        };
    }

    private static <T> T invalid(String key, String value, T fallback) {
        LOGGER.warn("Invalid value '{}' for {} in server.properties, using {}", value, key, fallback);
        return fallback;
    }
}
