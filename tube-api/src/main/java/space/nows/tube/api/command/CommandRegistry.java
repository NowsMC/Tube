package space.nows.tube.api.command;

import java.util.Collection;
import java.util.Optional;

public interface CommandRegistry {

    void register(String name, Command command, String... aliases);

    void unregister(String name);

    Collection<String> names();

    Optional<CommandInfo> find(String label);

    boolean dispatch(CommandSender sender, String line);
}
