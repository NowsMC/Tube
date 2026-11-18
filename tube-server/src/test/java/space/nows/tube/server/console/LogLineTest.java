package space.nows.tube.server.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.server.config.FileConfig;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

class LogLineTest {

    private static LoggingEvent event(String logger, Level level, String message, Throwable thrown) {
        LoggerContext context = new LoggerContext();
        LoggingEvent event = new LoggingEvent("test", context.getLogger(logger), level, message, thrown, null);
        event.setThreadName("main");
        event.setTimeStamp(LocalDateTime.of(2026, 11, 18, 8, 17, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return event;
    }

    @Test
    void keepsTubesOwnLineByDefault() {
        LogLine line = new LogLine(ConsoleSettings.defaults().file(), false);
        assertEquals("[08:17:00] [main/INFO]: Server started", line.format(event("tube", Level.INFO, "Server started", null)));
        assertEquals("[08:17:00] [main/WARN]: [Essentials] Slow", line.format(event("com.earth2me.Essentials", Level.WARN, "Slow", null)));
    }

    @Test
    void followsACustomFormat() {
        LogLine line = new LogLine(new LogStyle("{time} {level} {logger} {unknown} {message}", "HH:mm", Level.INFO), false);
        assertEquals("08:17 INFO tube {unknown} hi", line.format(event("tube", Level.INFO, "hi", null)));
    }

    @Test
    void stripsColorCodesWithoutColors() {
        LogLine line = new LogLine(new LogStyle("{message}", "HH:mm:ss", Level.INFO), false);
        assertEquals("Hello world", line.format(event("tube", Level.INFO, "§aHello §lworld", null)));
    }

    @Test
    void turnsColorCodesIntoTerminalColors() {
        LogLine line = new LogLine(new LogStyle("{message}", "HH:mm:ss", Level.INFO), true);
        String text = line.format(event("tube", Level.INFO, "§cred", null));
        assertTrue(text.contains("\u001B["), text);
        assertTrue(text.contains("red"), text);
    }

    @Test
    void appendsTheStackTrace() {
        LogLine line = new LogLine(ConsoleSettings.defaults().file(), false);
        String text = line.format(event("tube", Level.ERROR, "Broke", new IllegalStateException("boom")));
        assertTrue(text.startsWith("[08:17:00] [main/ERROR]: Broke" + System.lineSeparator()), text);
        assertTrue(text.contains("java.lang.IllegalStateException: boom"), text);
    }

    @Test
    void writesMissingSettingsAndReadsChangedOnes(@TempDir Path directory) {
        Path file = directory.resolve(ConsoleSettings.FILE_NAME);
        ConsoleSettings first = ConsoleSettings.load(file);
        assertEquals(ConsoleSettings.defaults(), first);

        FileConfig config = FileConfig.load(file);
        config.set("console.level", "debug");
        config.set("console.colors", "never");
        config.set("console.prompt", "tube> ");
        config.save();

        ConsoleSettings changed = ConsoleSettings.load(file);
        assertEquals(Level.DEBUG, changed.console().level());
        assertEquals(ColorMode.NEVER, changed.colors());
        assertEquals("tube> ", changed.prompt());
        assertEquals(Level.INFO, changed.file().level());
    }
}
