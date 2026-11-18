package space.nows.tube.api.plugin;

import java.util.List;
import java.util.Objects;

public record PluginCommand(String name, String description, String usage, List<String> aliases, String permission, String permissionMessage) {

    public PluginCommand {
        Objects.requireNonNull(name, "name");
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }
}
