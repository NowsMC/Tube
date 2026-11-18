package space.nows.tube.server.console;

import ch.qos.logback.classic.Level;
import space.nows.tube.server.config.FileConfig;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public record ConsoleSettings(LogStyle console, LogStyle file, ColorMode colors, String prompt, int historySize) {

    public static final String FILE_NAME = "tube.yml";
    static final String FORMAT = "[{time}] [{thread}/{level}]: {source}{message}";
    static final String TIME_FORMAT = "HH:mm:ss";

    public static ConsoleSettings defaults() {
        LogStyle style = new LogStyle(FORMAT, TIME_FORMAT, Level.INFO);
        return new ConsoleSettings(style, style, ColorMode.AUTO, "> ", 500);
    }

    public static ConsoleSettings load(Path file) {
        FileConfig config = FileConfig.load(file);
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("console.format", FORMAT);
        defaults.put("console.time-format", TIME_FORMAT);
        defaults.put("console.level", "info");
        defaults.put("console.colors", ColorMode.AUTO.key());
        defaults.put("console.prompt", "> ");
        defaults.put("console.history-size", 500);
        defaults.put("log-file.format", FORMAT);
        defaults.put("log-file.time-format", TIME_FORMAT);
        defaults.put("log-file.level", "info");
        boolean missing = false;
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
                missing = true;
            }
        }
        if (missing) {
            config.save();
        }
        return new ConsoleSettings(
                style(config, "console"),
                style(config, "log-file"),
                ColorMode.of(config.getString("console.colors", ColorMode.AUTO.key())),
                config.getString("console.prompt", "> "),
                Math.max(0, config.getInt("console.history-size", 500)));
    }

    private static LogStyle style(FileConfig config, String section) {
        return new LogStyle(
                config.getString(section + ".format", FORMAT),
                config.getString(section + ".time-format", TIME_FORMAT),
                Level.toLevel(config.getString(section + ".level", "info"), Level.INFO));
    }
}
