package space.nows.tube.server.command.builtin.info;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.plugin.PluginDescriptor;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.List;

final class VersionCommand extends ServerCommand {

    private final Server server;

    VersionCommand(Server server) {
        super("version", null, "version [plugin]", "ver");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        if (!input.hasNext()) {
            sender.sendMessage(server.name() + " " + server.version() + " (Minecraft " + server.minecraftVersion() + ", data version " + server.dataVersion() + ")");
            sender.sendMessage("Java " + Runtime.version().feature() + " on " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
            sender.sendMessage(server.plugins().size() + (server.plugins().size() == 1 ? " plugin" : " plugins") + " enabled");
            return;
        }
        String name = input.word();
        input.end();
        PluginDescriptor plugin = server.plugins().stream().filter(candidate -> candidate.name().equalsIgnoreCase(name)).findFirst()
                .orElseThrow(() -> new CommandFailure("No plugin named " + name));
        PluginSummary.lines(plugin).forEach(sender::sendMessage);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return args.size() == 1 ? Suggestions.matching(args.getFirst(), server.plugins().stream().map(PluginDescriptor::name).toList()) : List.of();
    }
}
