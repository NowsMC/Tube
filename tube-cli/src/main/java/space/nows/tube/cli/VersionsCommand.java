package space.nows.tube.cli;

import picocli.CommandLine.Command;

import space.nows.tube.SupportedVersions;

import java.util.concurrent.Callable;

@Command(name = "versions", description = "List supported Minecraft versions")
final class VersionsCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        SupportedVersions.minecraft().forEach(System.out::println);
        return 0;
    }
}
