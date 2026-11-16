package space.nows.tube.server;

import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

final class ScopedCommands implements CommandRegistry {

    private final CommandRegistry delegate;
    private final List<String> owned = new ArrayList<>();

    ScopedCommands(CommandRegistry delegate) {
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

    synchronized void unregisterAll() {
        owned.forEach(delegate::unregister);
        owned.clear();
    }
}
