package space.nows.tube.server.command.builtin;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.plugin.PluginDescriptor;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.stream.Collectors;

final class PluginsCommand extends ServerCommand {

    private final Server server;

    PluginsCommand(Server server) {
        super("plugins", null, "plugins", "pl");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String names = server.plugins().stream().map(PluginDescriptor::name).sorted().collect(Collectors.joining(", "));
        sender.sendMessage("Plugins (" + server.plugins().size() + "): " + names);
    }
}
