package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.Collection;
import java.util.function.Function;

final class HelpCommand extends ServerCommand {

    private final Function<CommandSender, Collection<String>> available;

    HelpCommand(Function<CommandSender, Collection<String>> available) {
        super("help", null, "help");
        this.available = available;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        sender.sendMessage("Commands: " + String.join(", ", available.apply(sender)));
    }
}
