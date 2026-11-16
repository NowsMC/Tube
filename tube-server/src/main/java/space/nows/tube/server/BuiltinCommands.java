package space.nows.tube.server;

import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.CommandSender;
import space.nows.tube.api.Player;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Server;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class BuiltinCommands {

    static final String ADMIN = "tube.admin";
    static final String OP = "tube.op";

    private BuiltinCommands() {
    }

    static void register(Server server, CommandRegistry registry) {
        registry.register("help", (sender, args) ->
                sender.sendMessage("Commands: " + String.join(", ", registry.names())));

        registry.register("version", (sender, args) ->
                sender.sendMessage(server.name() + " " + server.version() + " (Minecraft " + server.minecraftVersion() + ")"), "ver");

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

        registry.register("save-all", Command.withPermission(ADMIN, (sender, args) -> {
            server.worlds().saveAll();
            sender.sendMessage("Saved " + server.worlds().all().size() + " world(s)");
        }));

        registry.register("stop", Command.withPermission(ADMIN, (sender, args) -> {
            sender.sendMessage("Stopping");
            server.stop();
        }));

        registry.register("op", Command.withPermission(OP, (sender, args) -> changeOp(server, sender, args, true)));
        registry.register("deop", Command.withPermission(OP, (sender, args) -> changeOp(server, sender, args, false)));
    }

    private static void changeOp(Server server, CommandSender sender, List<String> args, boolean op) {
        if (args.size() != 1) {
            sender.sendMessage("Usage: " + (op ? "op" : "deop") + " <player>");
            return;
        }
        Optional<Player> target = server.players().stream().filter(player -> player.name().equalsIgnoreCase(args.getFirst())).findFirst();
        if (target.isEmpty()) {
            sender.sendMessage("Player " + args.getFirst() + " must be online");
            return;
        }
        server.permissions().setOp(target.get().uuid(), target.get().name(), op);
        sender.sendMessage((op ? "Made " : "Removed operator status from ") + target.get().name() + (op ? " an operator" : ""));
    }
}
