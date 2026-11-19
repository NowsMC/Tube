package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.config.ServerProperties;
import space.nows.tube.server.player.IdleKicker;

import java.nio.file.Path;
import java.util.List;

final class SetIdleTimeoutCommand extends ServerCommand {

    private final IdleKicker kicker;
    private final Path propertiesFile;

    SetIdleTimeoutCommand(IdleKicker kicker, Path propertiesFile) {
        super("setidletimeout", Permissions.command("setidletimeout"), "setidletimeout <minutes>");
        this.kicker = kicker;
        this.propertiesFile = propertiesFile;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        int minutes = input.integer();
        input.end();
        if (minutes < 0) {
            throw new CommandFailure("The idle timeout must not be negative");
        }
        kicker.setTimeoutMinutes(minutes);
        if (propertiesFile != null) {
            ServerProperties.set(propertiesFile, "player-idle-timeout", Integer.toString(minutes));
        }
        sender.sendMessage(minutes == 0 ? "Turned off the player idle timeout" : "Set the player idle timeout to " + minutes + " minutes");
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), "0", "5", "10", "30") : List.of();
    }
}
