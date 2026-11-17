package space.nows.tube.server.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.event.CommandDispatchEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.server.player.PlayerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandService implements CommandRegistry {

    static final String NO_PERMISSION = "<red>I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.";

    private record Entry(String name, Command handler, net.minestom.server.command.builder.Command bridge, List<String> labels) {
    }

    private final PlayerService players;
    private final ConsoleSender console = new ConsoleSender();
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private volatile EventBus events;

    public CommandService(PlayerService players) {
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
        args.setSuggestionCallback((sender, context, suggestion) -> offer(command, wrap(sender), suggestion));
        bridge.setCondition((sender, commandString) -> allowed(command, wrap(sender)));
        bridge.setDefaultExecutor((sender, context) -> run(command, wrap(sender), List.of()));
        bridge.addSyntax((sender, context) -> run(command, wrap(sender), List.of(context.get(args))), args);
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

    public void attachEvents(EventBus events) {
        this.events = events;
    }

    public void dispatchConsole(String line) {
        dispatch(console, line);
    }

    public void dispatch(CommandSender sender, String line) {
        EventBus bus = events;
        if (bus != null) {
            var posted = bus.post(new CommandDispatchEvent(sender, line));
            if (posted.isCancelled()) {
                return;
            }
            line = posted.line();
        }
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
            sender.sendMessage("Unknown command: " + parts[0]);
            return;
        }
        run(entry.handler(), sender, List.of(parts).subList(1, parts.length));
    }

    public List<String> suggest(CommandSender sender, String line) {
        CommandLine parsed = CommandLine.parse(line);
        Entry entry = entries.get(normalize(parsed.label()));
        if (entry == null || parsed.arguments().isEmpty() || !allowed(entry.handler(), sender)) {
            return List.of();
        }
        return entry.handler().suggest(sender, parsed.arguments());
    }

    private void offer(Command command, CommandSender sender, Suggestion suggestion) {
        if (!allowed(command, sender)) {
            return;
        }
        String input = suggestion.getInput();
        CommandLine parsed = CommandLine.parse(input.endsWith(" ") ? input.substring(0, input.length() - 1) : input);
        if (parsed.arguments().isEmpty()) {
            return;
        }
        suggestion.setStart(parsed.currentStart() + 1);
        suggestion.setLength(parsed.current().length());
        command.suggest(sender, parsed.arguments()).forEach(option -> suggestion.addEntry(new SuggestionEntry(option)));
    }

    private static boolean allowed(Command command, CommandSender sender) {
        return command.permission() == null || sender.hasPermission(command.permission());
    }

    private static void run(Command command, CommandSender sender, List<String> args) {
        if (!allowed(command, sender)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(NO_PERMISSION));
            return;
        }
        command.execute(sender, args);
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
