package space.nows.tube.server.console;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

final class LogSetup {

    private LogSetup() {
    }

    static void install(ConsoleSettings settings, TubeConsole console, Path logFile) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            return;
        }
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();
        root.setLevel(lowest(settings.console().level(), settings.file().level()));

        TerminalAppender terminal = new TerminalAppender(new LogLine(settings.console(), console.colors()), console);
        root.addAppender(start(terminal, context, "terminal", settings.console().level()));

        FileAppender<ILoggingEvent> file = new FileAppender<>();
        file.setFile(logFile.toString());
        file.setAppend(false);
        LogLineEncoder encoder = new LogLineEncoder(new LogLine(settings.file(), false));
        encoder.setContext(context);
        encoder.start();
        file.setEncoder(encoder);
        root.addAppender(start(file, context, "file", settings.file().level()));
    }

    private static Appender<ILoggingEvent> start(Appender<ILoggingEvent> appender, LoggerContext context, String name, Level level) {
        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(level.toString());
        filter.setContext(context);
        filter.start();
        appender.setName(name);
        appender.setContext(context);
        appender.addFilter(filter);
        appender.start();
        return appender;
    }

    private static Level lowest(Level first, Level second) {
        return first.isGreaterOrEqual(second) ? second : first;
    }
}
