package space.nows.tube.api.command;

import java.util.Collection;

public interface CommandRegistry {

    void register(String name, Command command, String... aliases);

    void unregister(String name);

    Collection<String> names();
}
