package space.nows.tube.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class CommandService implements CommandRegistry {

    private record Entry(String name, Command handler, net.minestom.server.command.builder.Command bridge, List<String> labels) {
    }

    private final PlayerService players;
    private final ConsoleSender console = new ConsoleSender();
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    CommandService(PlayerService players) {
        this.players = players;
    }

    @Override
    public synchronized void register(String name, Command command, String... aliases) {
        List<String> labels = new ArrayList<>();
        labels.add(normalize(name));
        Arrays.stream(aliases).map(CommandService::normalize).forEach(labels::add);
        for (String label : labels) {
            if (entries.containsKey(label)) {
                throw new IllegalArgumentException("Command already registered: " + label);
            }
        }

        var bridge = new net.minestom.server.command.builder.Command(labels.getFirst(), labels.stream().skip(1).toArray(String[]::new));
        var args = ArgumentType.StringArray("args");
        bridge.setDefaultExecutor((sender, context) -> command.execute(wrap(sender), List.of()));
        bridge.addSyntax((sender, context) -> command.execute(wrap(sender), List.of(context.get(args))), args);
        MinecraftServer.getCommandManager().register(bridge);

        Entry entry = new Entry(labels.getFirst(), command, bridge, List.copyOf(labels));
        labels.forEach(label -> entries.put(label, entry));
    }

    @Override
    public synchronized void unregister(String name) {
        Entry entry = entries.get(normalize(name));
        if (entry == null) {
            return;
        }
        entry.labels().forEach(entries::remove);
        MinecraftServer.getCommandManager().unregister(entry.bridge());
    }

    @Override
    public Collection<String> names() {
        return entries.values().stream().map(Entry::name).distinct().sorted().toList();
    }

    void dispatchConsole(String line) {
        String trimmed = line.strip();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.isEmpty()) {
            return;
        }
        String[] parts = trimmed.split("\\s+");
        Entry entry = entries.get(normalize(parts[0]));
        if (entry == null) {
            console.sendMessage("Unknown command: " + parts[0]);
            return;
        }
        entry.handler().execute(console, List.of(parts).subList(1, parts.length));
    }

    private CommandSender wrap(net.minestom.server.command.CommandSender sender) {
        if (sender instanceof net.minestom.server.entity.Player player) {
            return players.wrap(player);
        }
        return console;
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
