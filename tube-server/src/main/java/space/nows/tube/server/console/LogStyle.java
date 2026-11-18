package space.nows.tube.server.console;

import ch.qos.logback.classic.Level;

public record LogStyle(String format, String timeFormat, Level level) {
}
