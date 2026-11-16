package space.nows.tube.bukkit;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    @Test
    @Timeout(value = 180, unit = TimeUnit.SECONDS)
    void reportsHowRealPluginsBehave() throws IOException {
        assumeTrue(Files.isDirectory(SOURCE), "no .plugins folder next to the project");

        Path directory = Files.createTempDirectory("tube-real-plugins");
        Path plugins = Files.createDirectories(directory.resolve("plugins"));
        try (var jars = Files.list(SOURCE)) {
            for (Path jar : jars.filter(path -> path.getFileName().toString().endsWith(".jar")).toList()) {
                if (!jar.getFileName().toString().contains("(1)")) {
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
                        for (StackTraceElement frame : cause.getStackTrace()) {
                            if (frame.getClassName().startsWith("org.bukkit") || frame.getClassName().startsWith("io.papermc") || frame.getClassName().startsWith("space.nows")) {
                                chain.append("  @ ").append(frame.getClassName()).append('.').append(frame.getMethodName()).append(':').append(frame.getLineNumber());
                                break;
                            }
                        }
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
        try {
            runtime.loadPlugins();
            stub.scheduler.tick();
            report.add("== plugins that are enabled");
            for (Plugin plugin : runtime.pluginManager().getPlugins()) {
                report.add((plugin.isEnabled() ? "ENABLED  " : "DISABLED ") + plugin.getName() + " " + plugin.getDescription().getVersion());
            }
        } finally {
            runtime.shutdown();
            java.util.logging.Logger.getLogger("").removeHandler(collector);
        }
        report.add("== warnings and errors");
        report.addAll(problems);

        Path output = Path.of("target", "real-plugins-report.txt");
        Files.createDirectories(output.getParent());
        Files.write(output, report);
    }
}
