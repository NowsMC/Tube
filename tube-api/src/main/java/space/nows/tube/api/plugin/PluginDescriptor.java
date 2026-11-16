package space.nows.tube.api.plugin;

import java.util.List;
import java.util.Objects;

public record PluginDescriptor(String name, String version, String main, List<String> depends) {

    public PluginDescriptor {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(main, "main");
        depends = List.copyOf(depends);
    }
}
