package space.nows.tube.server.command.builtin.info;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.plugin.PluginDescriptor;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.List;
import java.util.Optional;

final class VersionCommand extends ServerCommand {

    private final Server server;

    VersionCommand(Server server) {
        super("version", null, "version [plugin]", "ver");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        if (!input.hasNext()) {
            sender.sendMessage(server.name() + " " + server.version() + " (Minecraft " + server.minecraftVersion() + ")");
            return;
        }
        String name = input.word();
        input.end();
        Optional<PluginDescriptor> found = server.plugins().stream().filter(plugin -> plugin.name().equalsIgnoreCase(name)).findFirst();
        if (found.isEmpty()) {
            sender.sendMessage("No plugin named " + name);
            return;
        }
        PluginDescriptor plugin = found.get();
        sender.sendMessage(plugin.name() + " " + plugin.version());
        if (plugin.description() != null && !plugin.description().isBlank()) {
            sender.sendMessage(plugin.description());
        }
        if (!plugin.authors().isEmpty()) {
            sender.sendMessage("Authors: " + String.join(", ", plugin.authors()));
        }
        if (plugin.website() != null && !plugin.website().isBlank()) {
            sender.sendMessage("Website: " + plugin.website());
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return args.size() == 1 ? Suggestions.matching(args.getFirst(), server.plugins().stream().map(PluginDescriptor::name).toList()) : List.of();
    }
}
