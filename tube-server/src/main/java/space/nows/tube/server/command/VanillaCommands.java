package space.nows.tube.server.command;

import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.CommandSender;
import space.nows.tube.api.GameMode;
import space.nows.tube.api.ItemStack;
import space.nows.tube.api.Location;
import space.nows.tube.api.Player;
import space.nows.tube.api.Server;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class VanillaCommands {

    private final Server server;

    public VanillaCommands(Server server) {
        this.server = server;
    }

    public void register(CommandRegistry registry) {
        registry.register("say", restricted("say", this::say));
        registry.register("msg", this::message, "tell", "w");
        registry.register("gamemode", restricted("gamemode", this::gamemode), "gm");
        registry.register("give", restricted("give", this::give));
        registry.register("tp", restricted("tp", this::teleport), "teleport");
        registry.register("summon", restricted("summon", this::summon));
    }

    private static Command restricted(String name, Command command) {
        return Command.withPermission("tube.command." + name, (sender, args) -> {
            try {
                command.execute(sender, args);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(e.getMessage());
            }
        });
    }

    private void say(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: say <message>");
            return;
        }
        server.broadcast("[" + sender.name() + "] " + String.join(" ", args));
    }

    private void message(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            sender.sendMessage("Usage: msg <player> <message>");
            return;
        }
        Optional<Player> target = player(args.getFirst());
        if (target.isEmpty()) {
            sender.sendMessage("Player " + args.getFirst() + " is not online");
            return;
        }
        String text = String.join(" ", args.subList(1, args.size()));
        sender.sendMessage("You whisper to " + target.get().name() + ": " + text);
        target.get().sendMessage(sender.name() + " whispers to you: " + text);
    }

    private void gamemode(CommandSender sender, List<String> args) {
        if (args.isEmpty() || args.size() > 2) {
            sender.sendMessage("Usage: gamemode <survival|creative|adventure|spectator> [player]");
            return;
        }
        GameMode mode = switch (args.getFirst().toLowerCase(Locale.ROOT)) {
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "adventure", "a", "2" -> GameMode.ADVENTURE;
            case "spectator", "sp", "3" -> GameMode.SPECTATOR;
            default -> throw new IllegalArgumentException("Unknown game mode " + args.getFirst());
        };
        Player target = args.size() == 2 ? requirePlayer(args.get(1)) : requireSelf(sender);
        target.setGameMode(mode);
        sender.sendMessage("Set " + target.name() + "'s game mode to " + mode.name().toLowerCase(Locale.ROOT));
    }

    private void give(CommandSender sender, List<String> args) {
        if (args.size() < 2 || args.size() > 3) {
            sender.sendMessage("Usage: give <player> <item> [amount]");
            return;
        }
        Player target = requirePlayer(args.getFirst());
        int amount = args.size() == 3 ? number(args.get(2)) : 1;
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must be at least 1");
        }
        ItemStack item = ItemStack.of(args.get(1), amount);
        boolean fitted = target.inventory().add(item);
        sender.sendMessage((fitted ? "Gave " : "Gave what fit of ") + amount + " " + item.type() + " to " + target.name());
    }

    private void teleport(CommandSender sender, List<String> args) {
        switch (args.size()) {
            case 1 -> requireSelf(sender).teleport(requirePlayer(args.getFirst()).location());
            case 2 -> requirePlayer(args.getFirst()).teleport(requirePlayer(args.get(1)).location());
            case 3 -> {
                Player self = requireSelf(sender);
                self.teleport(coordinates(self.location(), args.get(0), args.get(1), args.get(2)));
            }
            default -> {
                sender.sendMessage("Usage: tp <player> | tp <player> <target> | tp <x> <y> <z>");
                return;
            }
        }
        sender.sendMessage("Teleported");
    }

    private void summon(CommandSender sender, List<String> args) {
        if (args.size() != 1 && args.size() != 4) {
            sender.sendMessage("Usage: summon <entity> [x y z]");
            return;
        }
        Player self = requireSelf(sender);
        Location where = args.size() == 4 ? coordinates(self.location(), args.get(1), args.get(2), args.get(3)) : self.location();
        self.world().spawn(args.getFirst(), where);
        sender.sendMessage("Summoned " + args.getFirst());
    }

    private Optional<Player> player(String name) {
        return server.players().stream().filter(player -> player.name().equalsIgnoreCase(name)).findFirst();
    }

    private Player requirePlayer(String name) {
        return player(name).orElseThrow(() -> new IllegalArgumentException("Player " + name + " is not online"));
    }

    private static Player requireSelf(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        throw new IllegalArgumentException("This command needs a player target when run from the console");
    }

    private static Location coordinates(Location origin, String x, String y, String z) {
        return new Location(origin.world(), coordinate(x, origin.x()), coordinate(y, origin.y()), coordinate(z, origin.z()), origin.yaw(), origin.pitch());
    }

    private static double coordinate(String text, double current) {
        try {
            if (text.startsWith("~")) {
                return current + (text.length() == 1 ? 0 : Double.parseDouble(text.substring(1)));
            }
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate " + text);
        }
    }

    private static int number(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number " + text);
        }
    }
}
