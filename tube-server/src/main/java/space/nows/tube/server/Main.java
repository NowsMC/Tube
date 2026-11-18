package space.nows.tube.server;

import space.nows.tube.server.console.ConsoleSettings;
import space.nows.tube.server.console.TubeConsole;

import java.nio.file.Path;
import java.util.ServiceLoader;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws java.io.IOException {
        Path logs = java.nio.file.Files.createDirectories(Path.of("logs"));
        LogRotation.rotate(logs);
        TubeConsole console = TubeConsole.open(ConsoleSettings.load(Path.of(ConsoleSettings.FILE_NAME)), logs.resolve(LogRotation.LATEST));
        VersionProfile profile = ServiceLoader.load(VersionProfile.class, VersionProfile.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No version profile found on the classpath"));
        new DedicatedServer(profile, Path.of(""), console).start();
    }
}
