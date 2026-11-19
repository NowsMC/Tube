package space.nows.tube.bukkit;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

class RealPluginsTest {

    private static final Path SOURCE = Path.of("..", ".plugins");

    private static boolean selected(String jar) {
        String only = System.getProperty("tube.realPlugins", "");
        return only.isBlank() || java.util.Arrays.stream(only.split(",")).anyMatch(part -> jar.toLowerCase(java.util.Locale.ROOT).contains(part.strip().toLowerCase(java.util.Locale.ROOT)));
    }

    private static List<String> playScenario(StubServer stub) {
        List<String> output = new ArrayList<>();
        var login = stub.events.post(new space.nows.tube.api.event.player.PlayerPreLoginEvent("Steve", java.util.UUID.nameUUIDFromBytes("Steve".getBytes()),
                new java.net.InetSocketAddress("127.0.0.1", 50000), space.nows.tube.api.entity.player.LoginMode.OFFLINE));
        output.add("pre-login Steve: " + login.denyReason().map(reason -> "denied " + net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(reason)).orElse("allowed"));
        var player = stub.player("Steve");
        stub.events.post(new space.nows.tube.api.event.player.PlayerJoinEvent(player));
        for (int i = 0; i < 40; i++) {
            stub.scheduler.tick();
        }
        for (String line : System.getProperty("tube.realPlugins.commands", "").split(";")) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.strip().split(" ");
            var command = stub.commands.commands.get(parts[0]);
            if (command == null) {
                output.add("/" + line.strip() + ": no such command");
                continue;
            }
            List<String> replies = new ArrayList<>();
            try {
                command.execute(StubServer.consoleLike(replies), List.of(parts).subList(1, parts.length));
                for (int i = 0; i < 40; i++) {
                    stub.scheduler.tick();
                    Thread.sleep(50);
                }
            } catch (RuntimeException | LinkageError e) {
                replies.add("threw " + e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            output.add("/" + line.strip() + ": " + String.join(" | ", replies));
        }
        output.addAll(stub.playerMessages);
        return output;
    }

    @Test
    @Timeout(value = 180, unit = TimeUnit.SECONDS)
    void reportsHowRealPluginsBehave() throws IOException {
        assumeTrue(Files.isDirectory(SOURCE), "no .plugins folder next to the project");

        Path directory = Files.createTempDirectory("tube-real-plugins");
        Path plugins = Files.createDirectories(directory.resolve("plugins"));
        try (var jars = Files.list(SOURCE)) {
            for (Path jar : jars.filter(path -> path.getFileName().toString().endsWith(".jar")).toList()) {
                String name = jar.getFileName().toString();
                if (!name.contains("(1)") && selected(name)) {
                    Files.copy(jar, plugins.resolve(jar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        BukkitRuntime.forgetInstalledServer();
        List<String> problems = new ArrayList<>();
        Handler collector = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    StringBuilder chain = new StringBuilder();
                    for (Throwable cause = record.getThrown(); cause != null; cause = cause.getCause()) {
                        chain.append("\n      caused by ").append(cause.getClass().getName()).append(": ").append(String.valueOf(cause.getMessage()).lines().findFirst().orElse(""));
                        java.util.Arrays.stream(cause.getStackTrace())
                                .filter(frame -> !frame.getClassName().startsWith("java.") && !frame.getClassName().startsWith("jdk."))
                                .limit(3)
                                .forEach(frame -> chain.append("\n          at ").append(frame));
                    }
                    String firstCause = chain.toString();
                    problems.add("[" + record.getLevel() + "] " + record.getLoggerName() + ": " + record.getMessage() + firstCause);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        StubServer stub = new StubServer();
        BukkitRuntime runtime = BukkitRuntime.install(stub.server, plugins);
        java.util.logging.Logger.getLogger("").addHandler(collector);
        List<String> report = new ArrayList<>();
        List<String> scenarioOutput = new ArrayList<>();
        try {
            runtime.loadPlugins();
            stub.scheduler.tick();
            scenarioOutput.addAll(playScenario(stub));
            report.add("== plugins that are enabled");
            for (Plugin plugin : runtime.pluginManager().getPlugins()) {
                report.add((plugin.isEnabled() ? "ENABLED  " : "DISABLED ") + plugin.getName() + " " + plugin.getDescription().getVersion());
            }
        } finally {
            runtime.shutdown();
            java.util.logging.Logger.getLogger("").removeHandler(collector);
        }
        report.add("== scenario");
        report.addAll(scenarioOutput);
        report.add("== warnings and errors");
        report.addAll(problems);

        Path output = Path.of("target", "real-plugins-report.txt");
        Files.createDirectories(output.getParent());
        Files.write(output, report);
    }
}
