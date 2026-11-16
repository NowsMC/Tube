package space.nows.tube.server.command;

import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.CommandSender;
import space.nows.tube.api.Player;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Server;
import space.nows.tube.server.access.Profiles;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BuiltinCommands {

    static final String ADMIN = "tube.admin";
    static final String OP = "tube.op";

    private BuiltinCommands() {
    }

    public static void register(Server server, CommandRegistry registry, Profiles profiles) {
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
            server.saveAll();
            sender.sendMessage("Saved " + server.worlds().all().size() + " world(s) and " + server.players().size() + " player(s)");
        }));

        registry.register("stop", Command.withPermission(ADMIN, (sender, args) -> {
            sender.sendMessage("Stopping");
            server.stop();
        }));

        registry.register("op", Command.withPermission(OP, (sender, args) -> changeOp(server, profiles, sender, args, true)));
        registry.register("deop", Command.withPermission(OP, (sender, args) -> changeOp(server, profiles, sender, args, false)));
    }

    private static void changeOp(Server server, Profiles profiles, CommandSender sender, List<String> args, boolean op) {
        if (args.size() != 1) {
            sender.sendMessage("Usage: " + (op ? "op" : "deop") + " <player>");
            return;
        }
        Optional<Profiles.Profile> target = profiles.find(args.getFirst());
        if (target.isEmpty()) {
            sender.sendMessage("Unknown player " + args.getFirst());
            return;
        }
        server.permissions().setOp(target.get().uuid(), target.get().name(), op);
        sender.sendMessage((op ? "Made " : "Removed operator status from ") + target.get().name() + (op ? " an operator" : ""));
    }
}
