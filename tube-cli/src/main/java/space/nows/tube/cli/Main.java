package space.nows.tube.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import space.nows.tube.SupportedVersions;

@Command(
        name = "tube",
        mixinStandardHelpOptions = true,
        versionProvider = Main.TubeVersion.class,
        description = "Minecraft server launcher",
        subcommands = {VersionsCommand.class, RunCommand.class})
public final class Main {

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    static final class TubeVersion implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[] {"tube " + SupportedVersions.tube()};
        }
    }
}
