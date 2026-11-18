package space.nows.tube.server.command;

import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandInfo;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.plugin.PluginCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScopedCommands implements CommandRegistry {

    private final CommandRegistry delegate;
    private final Map<String, PluginCommand> declared;
    private final List<String> owned = new ArrayList<>();

    public ScopedCommands(CommandRegistry delegate) {
        this(delegate, List.of());
    }

    public ScopedCommands(CommandRegistry delegate, List<PluginCommand> declared) {
        this.delegate = delegate;
        this.declared = declared.stream().collect(Collectors.toMap(command -> normalize(command.name()), Function.identity(), (first, second) -> first));
    }

    @Override
    public synchronized void register(String name, Command command, String... aliases) {
        PluginCommand declaration = declared.get(normalize(name));
        if (declaration == null) {
            delegate.register(name, command, aliases);
        } else {
            Set<String> labels = new LinkedHashSet<>(List.of(aliases));
            labels.addAll(declaration.aliases());
            delegate.register(name, new DeclaredCommand(command, declaration), labels.toArray(String[]::new));
        }
        owned.add(normalize(name));
    }

    @Override
    public synchronized void unregister(String name) {
        delegate.unregister(name);
        owned.remove(normalize(name));
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
        return declared.keySet().stream().filter(name -> !owned.contains(name)).sorted().toList();
    }

    public synchronized void unregisterAll() {
        owned.forEach(delegate::unregister);
        owned.clear();
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
