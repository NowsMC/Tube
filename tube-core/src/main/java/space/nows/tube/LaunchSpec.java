package space.nows.tube;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record LaunchSpec(Path java, Path jar, Path workDir, List<String> jvmArgs) {

    public static final int RESTART_EXIT_CODE = 100;
    public static final String RESTART_PROPERTY = "tube.restart.exit";

    public LaunchSpec {
        Objects.requireNonNull(java, "java");
        Objects.requireNonNull(jar, "jar");
        Objects.requireNonNull(workDir, "workDir");
        jvmArgs = List.copyOf(jvmArgs);
    }

    public List<String> command() {
        List<String> command = new ArrayList<>();
        command.add(java.toString());
        command.add("-D" + RESTART_PROPERTY + "=" + RESTART_EXIT_CODE);
        command.addAll(jvmArgs);
        command.add("-jar");
        command.add(jar.toAbsolutePath().toString());
        command.add("nogui");
        return List.copyOf(command);
    }

    public Process start() throws IOException {
        if (!Files.isRegularFile(jar)) {
            throw new IOException("Server jar not found: " + jar);
        }
        Files.createDirectories(workDir);
        return new ProcessBuilder(command())
                .directory(workDir.toFile())
                .inheritIO()
                .start();
    }
}
