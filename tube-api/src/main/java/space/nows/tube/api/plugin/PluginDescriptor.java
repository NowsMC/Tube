package space.nows.tube.api.plugin;

import java.util.List;
import java.util.Objects;

public record PluginDescriptor(
        String name,
        String version,
        String main,
        String description,
        List<String> authors,
        String website,
        String prefix,
        List<String> depends,
        List<String> softDepends,
        List<String> loadBefore,
        String tube,
        List<String> minecraft,
        List<PluginCommand> commands,
        List<PluginPermission> permissions) {

    public PluginDescriptor {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(main, "main");
        authors = copy(authors);
        depends = copy(depends);
        softDepends = copy(softDepends);
        loadBefore = copy(loadBefore);
        minecraft = copy(minecraft);
        commands = copy(commands);
        permissions = copy(permissions);
    }

    public PluginDescriptor(String name, String version, String main, List<String> depends) {
        this(name, version, main, null, null, null, null, depends, null, null, null, null, null, null);
    }

    public String logName() {
        return prefix == null || prefix.isBlank() ? name : prefix;
    }

    private static <T> List<T> copy(List<T> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}
