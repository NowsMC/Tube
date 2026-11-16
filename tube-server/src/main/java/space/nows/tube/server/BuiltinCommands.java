package space.nows.tube.server;

import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.Player;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Server;

import java.util.stream.Collectors;

final class BuiltinCommands {

    private BuiltinCommands() {
    }

    static void register(Server server, CommandRegistry registry) {
        registry.register("help", (sender, args) ->
                sender.sendMessage("Commands: " + String.join(", ", registry.names())));

        registry.register("list", (sender, args) -> {
            String names = server.players().stream().map(Player::name).sorted().collect(Collectors.joining(", "));
            sender.sendMessage("Online (" + server.players().size() + "): " + names);
        });

        registry.register("plugins", (sender, args) -> {
            String names = server.plugins().stream()
                    .map(PluginDescriptor::name)
                    .sorted()
                    .collect(Collectors.joining(", "));
            sender.sendMessage("Plugins (" + server.plugins().size() + "): " + names);
        });

        registry.register("save-all", (sender, args) -> {
            server.worlds().saveAll();
            sender.sendMessage("Saved " + server.worlds().all().size() + " world(s)");
        });

        registry.register("stop", (sender, args) -> {
            sender.sendMessage("Stopping");
            server.stop();
        });
    }
}
