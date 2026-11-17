package space.nows.tube.server.command.support;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class Targets {

    public static final List<String> PLAYER_SELECTORS = List.of("@a", "@p", "@r", "@s");
    public static final List<String> ENTITY_SELECTORS = List.of("@a", "@e", "@p", "@r", "@s");

    private final Server server;

    public Targets(Server server) {
        this.server = server;
    }

    public Optional<Player> online(String name) {
        return server.players().stream().filter(player -> player.name().equalsIgnoreCase(name)).findFirst();
    }

    public Player player(CommandSender sender, String text) {
        List<Player> found = players(sender, text);
        if (found.size() != 1) {
            throw new CommandFailure("Only one player is allowed, but the selector matched " + found.size());
        }
        return found.getFirst();
    }

    public List<Player> players(CommandSender sender, String text) {
        List<Player> found = switch (text) {
            case "@a" -> List.copyOf(server.players());
            case "@s" -> List.of(self(sender));
            case "@p" -> nearest(sender).map(List::of).orElse(List.of());
            case "@r" -> random();
            default -> online(text).map(List::of).orElseThrow(() -> new CommandFailure("Player " + text + " is not online"));
        };
        if (found.isEmpty()) {
            throw new CommandFailure("No player was found");
        }
        return found;
    }

    public List<Entity> entities(CommandSender sender, String text) {
        if (!text.equals("@e")) {
            return List.copyOf(players(sender, text));
        }
        List<Entity> found = new ArrayList<>();
        server.worlds().all().forEach(world -> found.addAll(world.entities()));
        if (found.isEmpty()) {
            throw new CommandFailure("No entity was found");
        }
        return found;
    }

    public List<String> playerNames() {
        return server.players().stream().map(Player::name).toList();
    }

    public List<String> playerOptions() {
        List<String> options = new ArrayList<>(playerNames());
        options.addAll(PLAYER_SELECTORS);
        return options;
    }

    public List<String> entityOptions() {
        List<String> options = new ArrayList<>(playerNames());
        options.addAll(ENTITY_SELECTORS);
        return options;
    }

    public static Player self(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        throw new CommandFailure("This command needs a player target when run from the console");
    }

    public Location origin(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.location();
        }
        return server.worlds().defaultWorld().spawn();
    }

    private Optional<Player> nearest(CommandSender sender) {
        if (!(sender instanceof Player self)) {
            return server.players().stream().findFirst();
        }
        Location from = self.location();
        return server.players().stream()
                .filter(player -> player.world() == from.world())
                .min(Comparator.comparingDouble(player -> distanceSquared(player.location(), from)));
    }

    private List<Player> random() {
        List<Player> online = List.copyOf(server.players());
        return online.isEmpty() ? List.of() : List.of(online.get(ThreadLocalRandom.current().nextInt(online.size())));
    }

    private static double distanceSquared(Location a, Location b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
