package space.nows.tube.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.GameMode;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

record ServerSettings(
        String serverIp,
        int serverPort,
        boolean onlineMode,
        String motd,
        int maxPlayers,
        String levelName,
        GameMode gamemode,
        Difficulty difficulty,
        int autosaveSeconds) {

    enum Difficulty {
        PEACEFUL,
        EASY,
        NORMAL,
        HARD
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private static final Map<String, String> DEFAULTS = defaults();

    private static Map<String, String> defaults() {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("server-ip", "");
        defaults.put("server-port", "25565");
        defaults.put("online-mode", "true");
        defaults.put("motd", "A Tube Server");
        defaults.put("max-players", "20");
        defaults.put("level-name", "world");
        defaults.put("gamemode", "survival");
        defaults.put("difficulty", "easy");
        defaults.put("autosave-seconds", "300");
        return defaults;
    }

    static ServerSettings load(Path file) {
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
        return new ServerSettings(
                text(properties, "server-ip"),
                integer(properties, "server-port", 1, 65535),
                bool(properties, "online-mode"),
                text(properties, "motd"),
                integer(properties, "max-players", 1, Integer.MAX_VALUE),
                levelName(properties),
                gameMode(properties),
                difficulty(properties),
                integer(properties, "autosave-seconds", 0, Integer.MAX_VALUE));
    }

    String bindAddress() {
        return serverIp.isBlank() ? "0.0.0.0" : serverIp;
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

    private static String text(Properties properties, String key) {
        return properties.getProperty(key, DEFAULTS.get(key)).strip();
    }

    private static boolean bool(Properties properties, String key) {
        String value = text(properties, key);
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        return invalid(key, value, Boolean.parseBoolean(DEFAULTS.get(key)));
    }

    private static int integer(Properties properties, String key, int min, int max) {
        String value = text(properties, key);
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

    private static String levelName(Properties properties) {
        String value = text(properties, "level-name");
        return value.matches("[A-Za-z0-9_-]+") ? value : invalid("level-name", value, DEFAULTS.get("level-name"));
    }

    private static GameMode gameMode(Properties properties) {
        String value = text(properties, "gamemode").toLowerCase(Locale.ROOT);
        return switch (value) {
            case "survival", "0" -> GameMode.SURVIVAL;
            case "creative", "1" -> GameMode.CREATIVE;
            case "adventure", "2" -> GameMode.ADVENTURE;
            case "spectator", "3" -> GameMode.SPECTATOR;
            default -> invalid("gamemode", value, GameMode.SURVIVAL);
        };
    }

    private static Difficulty difficulty(Properties properties) {
        String value = text(properties, "difficulty").toLowerCase(Locale.ROOT);
        return switch (value) {
            case "peaceful", "0" -> Difficulty.PEACEFUL;
            case "easy", "1" -> Difficulty.EASY;
            case "normal", "2" -> Difficulty.NORMAL;
            case "hard", "3" -> Difficulty.HARD;
            default -> invalid("difficulty", value, Difficulty.EASY);
        };
    }

    private static <T> T invalid(String key, String value, T fallback) {
        LOGGER.warn("Invalid value '{}' for {} in server.properties, using {}", value, key, fallback);
        return fallback;
    }
}
