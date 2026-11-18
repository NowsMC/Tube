package space.nows.tube.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import space.nows.tube.LaunchSpec;
import space.nows.tube.SupportedVersions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "run", description = "Start a Minecraft server")
final class RunCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "<version>", description = "Minecraft version")
    private String version;

    @Option(names = "--jar", required = true, description = "Server jar to start")
    private Path jar;

    @Option(names = "--dir", defaultValue = ".", description = "Server directory (default: ${DEFAULT-VALUE})")
    private Path dir;

    @Option(names = "--java", defaultValue = "java", description = "Java executable (default: ${DEFAULT-VALUE})")
    private Path java;

    @Option(names = "--jvm-arg", description = "Extra JVM argument, repeatable")
    private List<String> jvmArgs = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        if (SupportedVersions.find(version).isEmpty()) {
            System.err.println("Unsupported version " + version + ". Supported: " + String.join(", ", SupportedVersions.minecraft()));
            return 2;
        }
        LaunchSpec spec = new LaunchSpec(java, jar, dir, jvmArgs);
        int exit;
        do {
            exit = spec.start().waitFor();
        } while (exit == LaunchSpec.RESTART_EXIT_CODE);
        return exit;
    }
}
