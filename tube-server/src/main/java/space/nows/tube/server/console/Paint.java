package space.nows.tube.server.console;

import ch.qos.logback.classic.Level;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.regex.Pattern;

final class Paint {

    private static final String RESET = "\u001B[0m";
    private static final String GRAY = "\u001B[90m";
    private static final String CYAN = "\u001B[36m";
    private static final Pattern SECTION_CODE = Pattern.compile("(?i)§[0-9a-fk-orx]");
    private static final Map<Level, String> LEVELS = Map.of(
            Level.ERROR, "\u001B[31m",
            Level.WARN, "\u001B[33m",
            Level.INFO, "\u001B[32m",
            Level.DEBUG, CYAN,
            Level.TRACE, GRAY);
    private static final Map<Level, String> MESSAGES = Map.of(
            Level.ERROR, "\u001B[31m",
            Level.WARN, "\u001B[33m");

    private Paint() {
    }

    static String muted(String text) {
        return GRAY + text + RESET;
    }

    static String source(String text) {
        return CYAN + text + RESET;
    }

    static String level(Level level, String text) {
        return LEVELS.getOrDefault(level, "") + text + RESET;
    }

    static String message(Level level, String text) {
        String body = text.indexOf('§') < 0 ? text
                : ANSIComponentSerializer.ansi().serialize(LegacyComponentSerializer.legacySection().deserialize(text));
        String tint = MESSAGES.get(level);
        return tint == null ? body : tint + body + RESET;
    }

    static String plain(String text) {
        return text.indexOf('§') < 0 ? text : SECTION_CODE.matcher(text).replaceAll("");
    }
}
