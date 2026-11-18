package space.nows.tube.server.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.PermissionDefault;
import space.nows.tube.api.plugin.PluginCommand;
import space.nows.tube.api.plugin.PluginDescriptor;
import space.nows.tube.api.plugin.PluginPermission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class DescriptorReader {

    static final String FILE_NAME = "tube-plugin";

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_.-]+");
    private static final Map<String, List<String>> KEYS = Map.of(
            "authors", List.of("authors", "author"),
            "depends", List.of("depends", "depend"),
            "soft-depends", List.of("soft-depends", "softdepend"),
            "load-before", List.of("load-before", "loadbefore"),
            "permission-message", List.of("permission-message", "permissionMessage"));

    private DescriptorReader() {
    }

    static PluginDescriptor read(Map<?, ?> map) {
        String name = required(map, "name");
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("invalid plugin name " + name);
        }
        if (map.containsKey("libraries")) {
            LOGGER.warn("Plugin {} lists libraries, which Tube does not download; shade them into the plugin jar", name);
        }
        return new PluginDescriptor(
                name,
                required(map, "version"),
                required(map, "main"),
                text(map, "description"),
                list(map, "authors"),
                text(map, "website"),
                text(map, "prefix"),
                list(map, "depends"),
                list(map, "soft-depends"),
                list(map, "load-before"),
                text(map, "tube"),
                list(map, "minecraft"),
                commands(map.get("commands")),
                permissions(map.get("permissions")));
    }

    private static List<PluginCommand> commands(Object value) {
        List<PluginCommand> commands = new ArrayList<>();
        if (value instanceof Map<?, ?> all) {
            for (Map.Entry<?, ?> entry : all.entrySet()) {
                Map<?, ?> details = entry.getValue() instanceof Map<?, ?> map ? map : Map.of();
                commands.add(new PluginCommand(String.valueOf(entry.getKey()), text(details, "description"), text(details, "usage"),
                        list(details, "aliases"), text(details, "permission"), text(details, "permission-message")));
            }
        }
        return commands;
    }

    private static List<PluginPermission> permissions(Object value) {
        List<PluginPermission> permissions = new ArrayList<>();
        if (value instanceof Map<?, ?> all) {
            for (Map.Entry<?, ?> entry : all.entrySet()) {
                Map<?, ?> details = entry.getValue() instanceof Map<?, ?> map ? map : Map.of();
                permissions.add(new PluginPermission(String.valueOf(entry.getKey()), text(details, "description"),
                        PermissionDefault.parse(text(details, "default"), PermissionDefault.OP), children(details.get("children"))));
            }
        }
        return permissions;
    }

    private static Map<String, Boolean> children(Object value) {
        Map<String, Boolean> children = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((node, granted) -> children.put(String.valueOf(node), !"false".equalsIgnoreCase(String.valueOf(granted))));
        } else if (value instanceof List<?> nodes) {
            nodes.forEach(node -> children.put(String.valueOf(node), true));
        }
        return children;
    }

    private static Object find(Map<?, ?> map, String key) {
        for (String name : KEYS.getOrDefault(key, List.of(key))) {
            Object value = map.get(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String text(Map<?, ?> map, String key) {
        Object value = find(map, key);
        return value == null || String.valueOf(value).isBlank() ? null : String.valueOf(value);
    }

    private static List<String> list(Map<?, ?> map, String key) {
        Object value = find(map, key);
        if (value instanceof List<?> items) {
            return items.stream().map(String::valueOf).toList();
        }
        return value == null || String.valueOf(value).isBlank() ? List.of() : List.of(String.valueOf(value));
    }

    private static String required(Map<?, ?> map, String key) {
        String value = text(map, key);
        if (value == null) {
            throw new IllegalArgumentException(FILE_NAME + " is missing " + key);
        }
        return value;
    }
}
