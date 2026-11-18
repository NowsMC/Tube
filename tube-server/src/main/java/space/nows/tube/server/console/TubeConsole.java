package space.nows.tube.server.console;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import space.nows.tube.api.scheduler.Scheduler;
import space.nows.tube.server.command.CommandService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class TubeConsole {

    private final ConsoleSettings settings;
    private final PrintStream out = System.out;
    private final Terminal terminal;
    private final LineReader reader;
    private final boolean colors;
    private volatile boolean closed;
    private volatile CommandService commands;

    private TubeConsole(ConsoleSettings settings) {
        this.settings = settings;
        this.terminal = openTerminal();
        boolean capable = terminal != null && !Terminal.TYPE_DUMB.equals(terminal.getType());
        this.colors = settings.colors() == ColorMode.ALWAYS || settings.colors() == ColorMode.AUTO && capable;
        this.reader = capable ? LineReaderBuilder.builder()
                .terminal(terminal)
                .appName("Tube")
                .completer(new CommandCompleter(this))
                .highlighter(new CommandHighlighter(this))
                .variable(LineReader.HISTORY_SIZE, settings.historySize())
                .build() : null;
    }

    public static TubeConsole open(ConsoleSettings settings, Path logFile) {
        TubeConsole console = new TubeConsole(settings);
        LogSetup.install(settings, console, logFile);
        System.setOut(new PrintStream(new LoggedStream(LoggerFactory.getLogger("STDOUT"), Level.INFO), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new LoggedStream(LoggerFactory.getLogger("STDERR"), Level.WARN), true, StandardCharsets.UTF_8));
        Runtime.getRuntime().addShutdownHook(new Thread(console::close, "Console shutdown"));
        return console;
    }

    public void start(Scheduler scheduler, CommandService commands) {
        this.commands = commands;
        Thread thread = new Thread(() -> read(scheduler, commands), "Console");
        thread.setDaemon(true);
        thread.start();
    }

    boolean colors() {
        return colors;
    }

    CommandService commands() {
        return commands;
    }

    void print(String text) {
        if (reader != null && !closed) {
            reader.printAbove(text);
            return;
        }
        out.println(text);
        out.flush();
    }

    private void read(Scheduler scheduler, CommandService commands) {
        Logger logger = LoggerFactory.getLogger("tube");
        BufferedReader plain = reader == null ? new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)) : null;
        while (!closed) {
            String line;
            try {
                line = reader != null ? reader.readLine(settings.prompt()) : plain.readLine();
            } catch (UserInterruptException e) {
                scheduler.runSync(() -> commands.dispatchConsole("stop"));
                return;
            } catch (EndOfFileException e) {
                return;
            } catch (IOException e) {
                logger.warn("Console input closed", e);
                return;
            }
            if (line == null) {
                return;
            }
            if (!line.isBlank()) {
                scheduler.runSync(() -> dispatch(logger, commands, line));
            }
        }
    }

    private static void dispatch(Logger logger, CommandService commands, String line) {
        try {
            commands.dispatchConsole(line);
        } catch (RuntimeException e) {
            logger.error("Command failed: {}", line, e);
        }
    }

    private void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (terminal != null) {
            try {
                terminal.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static Terminal openTerminal() {
        try {
            return TerminalBuilder.builder().name("Tube").system(true).dumb(true).build();
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
