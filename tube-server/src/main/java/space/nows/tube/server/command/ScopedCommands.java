package space.nows.tube.server.command;

import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class ScopedCommands implements CommandRegistry {

    private final CommandRegistry delegate;
    private final List<String> owned = new ArrayList<>();

    public ScopedCommands(CommandRegistry delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized void register(String name, Command command, String... aliases) {
        delegate.register(name, command, aliases);
        owned.add(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public synchronized void unregister(String name) {
        delegate.unregister(name);
        owned.remove(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public Collection<String> names() {
        return delegate.names();
    }

    public synchronized void unregisterAll() {
        owned.forEach(delegate::unregister);
        owned.clear();
    }
}
