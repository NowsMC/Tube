package space.nows.tube.api.plugin;

import space.nows.tube.api.permission.PermissionDefault;

import java.util.Map;
import java.util.Objects;

public record PluginPermission(String node, String description, PermissionDefault fallback, Map<String, Boolean> children) {

    public PluginPermission {
        Objects.requireNonNull(node, "node");
        fallback = fallback == null ? PermissionDefault.OP : fallback;
        children = children == null ? Map.of() : Map.copyOf(children);
    }
}
