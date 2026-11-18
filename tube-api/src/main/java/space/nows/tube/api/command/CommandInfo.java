package space.nows.tube.api.command;

import java.util.List;
import java.util.Objects;

public record CommandInfo(String name, List<String> aliases, String description, String usage, String permission) {

    public CommandInfo {
        Objects.requireNonNull(name, "name");
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }
}
