package space.nows.tube.server;

import java.nio.file.Path;
import java.util.ServiceLoader;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws java.io.IOException {
        Path logs = java.nio.file.Files.createDirectories(Path.of("logs"));
        LogRotation.rotate(logs);
        VersionProfile profile = ServiceLoader.load(VersionProfile.class, VersionProfile.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No version profile found on the classpath"));
        new TubeServer(profile, Path.of("")).start();
    }
}
