package space.nows.tube.server.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandInfo;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.plugin.PluginCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScopedCommands implements CommandRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final CommandRegistry delegate;
    private final String owner;
    private final Map<String, PluginCommand> declared;
    private final Map<String, String> owned = new LinkedHashMap<>();

    public ScopedCommands(CommandRegistry delegate, String owner, List<PluginCommand> declared) {
        this.delegate = delegate;
        this.owner = normalize(owner).replaceAll("\\s+", "-");
        this.declared = declared.stream().collect(Collectors.toMap(command -> normalize(command.name()), Function.identity(), (first, second) -> first));
    }

    @Override
    public synchronized void register(String name, Command command, String... aliases) {
        String key = normalize(name);
        if (owned.containsKey(key)) {
            throw new IllegalArgumentException("Command already registered by " + owner + ": " + name);
        }
        PluginCommand declaration = declared.get(key);
        Set<String> labels = new LinkedHashSet<>();
        labels.add(key);
        for (String alias : aliases) {
            labels.add(normalize(alias));
        }
        if (declaration != null) {
            declaration.aliases().forEach(alias -> labels.add(normalize(alias)));
        }
        Command handler = declaration == null ? command : new DeclaredCommand(command, declaration);

        String qualified = owner + ":" + key;
        List<String> free = new ArrayList<>(labels.stream().filter(label -> delegate.find(label).isEmpty()).toList());
        String primary = key;
        if (!free.remove(key)) {
            primary = qualified;
            LOGGER.warn("Command /{} of plugin {} is taken by another command, use /{} instead", key, owner, qualified);
        } else if (delegate.find(qualified).isEmpty()) {
            free.add(qualified);
        }
        for (String label : labels) {
            if (!free.contains(label) && !label.equals(key)) {
                LOGGER.warn("Alias /{} of plugin {} command {} is taken by another command and was skipped", label, owner, key);
            }
        }
        delegate.register(primary, handler, free.toArray(String[]::new));
        owned.put(key, primary);
    }

    @Override
    public synchronized void unregister(String name) {
        String label = normalize(name);
        String key = owned.containsKey(label) ? label
                : owned.entrySet().stream().filter(entry -> entry.getValue().equals(label)).map(Map.Entry::getKey).findFirst().orElse(null);
        if (key == null) {
            throw new IllegalArgumentException("Plugin " + owner + " did not register command " + name);
        }
        delegate.unregister(owned.remove(key));
    }

    @Override
    public Collection<String> names() {
        return delegate.names();
    }

    @Override
    public Optional<CommandInfo> find(String label) {
        return delegate.find(label);
    }

    @Override
    public boolean dispatch(CommandSender sender, String line) {
        return delegate.dispatch(sender, line);
    }

    public synchronized List<String> declaredButMissing() {
        return declared.keySet().stream().filter(name -> !owned.containsKey(name)).sorted().toList();
    }

    public synchronized void unregisterAll() {
        owned.values().forEach(delegate::unregister);
        owned.clear();
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
