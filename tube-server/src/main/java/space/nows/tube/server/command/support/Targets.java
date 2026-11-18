package space.nows.tube.server.command.support;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        List<Player> found;
        if (!Selector.is(text)) {
            found = online(text).map(List::of).orElseThrow(() -> new CommandFailure("Player " + text + " is not online"));
        } else {
            Selector selector = Selector.parse(text);
            List<Player> candidates = switch (selector.base()) {
                case "@a", "@r" -> List.copyOf(server.players());
                case "@p" -> sender instanceof Player self
                        ? server.players().stream().filter(player -> player.world() == self.world()).toList()
                        : List.copyOf(server.players());
                case "@s" -> List.of(self(sender));
                default -> throw new CommandFailure("Unknown selector " + selector.base());
            };
            found = selector.apply(candidates, origin(sender));
        }
        if (found.isEmpty()) {
            throw new CommandFailure("No player was found");
        }
        return found;
    }

    public List<Entity> entities(CommandSender sender, String text) {
        if (!text.startsWith("@e")) {
            return List.copyOf(players(sender, text));
        }
        Selector selector = Selector.parse(text);
        if (!selector.base().equals("@e")) {
            throw new CommandFailure("Unknown selector " + selector.base());
        }
        List<Entity> candidates = new ArrayList<>();
        server.worlds().all().forEach(world -> candidates.addAll(world.entities()));
        List<Entity> found = selector.apply(candidates, origin(sender));
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

    public List<String> suggestPlayers(String typed) {
        return suggest(typed, PLAYER_SELECTORS, playerOptions());
    }

    public List<String> suggestEntities(String typed) {
        return suggest(typed, ENTITY_SELECTORS, entityOptions());
    }

    private static List<String> suggest(String typed, List<String> selectors, List<String> options) {
        if (typed.startsWith("@") && typed.indexOf('[') >= 0 || selectors.stream().anyMatch(typed::equalsIgnoreCase)) {
            return Selector.suggest(typed, selectors, RegistryNames.entities());
        }
        return Suggestions.matching(typed, options);
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
}
