package space.nows.tube.server.console;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class LogLine {

    private static final Set<String> QUIET_SOURCES = Set.of("tube", "ServerProcessImpl");

    private final List<Function<ILoggingEvent, String>> parts = new ArrayList<>();
    private final DateTimeFormatter time;
    private final boolean colors;

    public LogLine(LogStyle style, boolean colors) {
        this.time = timeFormat(style.timeFormat());
        this.colors = colors;
        Map<String, Function<ILoggingEvent, String>> tokens = Map.of(
                "time", this::time,
                "thread", ILoggingEvent::getThreadName,
                "level", this::level,
                "source", this::source,
                "logger", ILoggingEvent::getLoggerName,
                "message", this::message);
        compile(style.format(), tokens);
    }

    public String format(ILoggingEvent event) {
        StringBuilder line = new StringBuilder();
        for (Function<ILoggingEvent, String> part : parts) {
            line.append(part.apply(event));
        }
        IThrowableProxy thrown = event.getThrowableProxy();
        if (thrown != null) {
            line.append(System.lineSeparator()).append(ThrowableProxyUtil.asString(thrown).stripTrailing());
        }
        return line.toString();
    }

    private void compile(String format, Map<String, Function<ILoggingEvent, String>> tokens) {
        int index = 0;
        while (index < format.length()) {
            int open = format.indexOf('{', index);
            int close = open < 0 ? -1 : format.indexOf('}', open);
            if (close < 0) {
                literal(format.substring(index));
                return;
            }
            literal(format.substring(index, open));
            Function<ILoggingEvent, String> token = tokens.get(format.substring(open + 1, close));
            if (token == null) {
                literal(format.substring(open, close + 1));
            } else {
                parts.add(token);
            }
            index = close + 1;
        }
    }

    private void literal(String text) {
        if (!text.isEmpty()) {
            parts.add(event -> text);
        }
    }

    private String time(ILoggingEvent event) {
        String text = time.format(Instant.ofEpochMilli(event.getTimeStamp()));
        return colors ? Paint.muted(text) : text;
    }

    private String level(ILoggingEvent event) {
        String text = event.getLevel().toString();
        return colors ? Paint.level(event.getLevel(), text) : text;
    }

    private String source(ILoggingEvent event) {
        String name = event.getLoggerName();
        String shortName = name.substring(name.lastIndexOf('.') + 1);
        if (QUIET_SOURCES.contains(shortName)) {
            return "";
        }
        String text = "[" + shortName + "] ";
        return colors ? Paint.source(text) : text;
    }

    private String message(ILoggingEvent event) {
        String text = event.getFormattedMessage();
        if (text == null) {
            return "";
        }
        return colors ? Paint.message(event.getLevel(), text) : Paint.plain(text);
    }

    private static DateTimeFormatter timeFormat(String pattern) {
        try {
            return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
        } catch (IllegalArgumentException e) {
            return DateTimeFormatter.ofPattern(ConsoleSettings.TIME_FORMAT).withZone(ZoneId.systemDefault());
        }
    }
}
