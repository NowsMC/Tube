package space.nows.tube.server;

import java.nio.file.Path;
import java.util.ServiceLoader;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        VersionProfile profile = ServiceLoader.load(VersionProfile.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No version profile found on the classpath"));
        new TubeServer(profile, Path.of("")).start();
    }
}
