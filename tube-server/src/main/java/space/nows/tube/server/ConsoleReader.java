package space.nows.tube.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.Scheduler;
import space.nows.tube.server.command.CommandService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class ConsoleReader {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private ConsoleReader() {
    }

    static void start(Scheduler scheduler, CommandService commands) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String input = line;
                    scheduler.runSync(() -> dispatch(commands, input));
                }
            } catch (IOException e) {
                LOGGER.warn("Console input closed", e);
            }
        }, "tube-console");
        thread.setDaemon(true);
        thread.start();
    }

    private static void dispatch(CommandService commands, String line) {
        try {
            commands.dispatchConsole(line);
        } catch (RuntimeException e) {
            LOGGER.error("Command failed", e);
        }
    }
}
