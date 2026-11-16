package org.bukkit.plugin;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PluginDescriptionFile {

    private static final Yaml YAML = new Yaml();

    private String name;
    private String main;
    private String version;
    private String description;
    private String website;
    private String prefix;
    private String apiVersion;
    private PluginLoadOrder load = PluginLoadOrder.POSTWORLD;
    private List<String> authors = List.of();
    private List<String> depend = List.of();
    private List<String> softDepend = List.of();
    private List<String> loadBefore = List.of();
    private Map<String, Map<String, Object>> commands = Map.of();
    private List<Permission> permissions = List.of();
    private PermissionDefault defaultPermission = PermissionDefault.OP;

    public PluginDescriptionFile(String name, String version, String main) {
        this.name = name.replace(' ', '_');
        this.version = version;
        this.main = main;
    }

    public PluginDescriptionFile(InputStream stream) throws InvalidDescriptionException {
        this(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public PluginDescriptionFile(Reader reader) throws InvalidDescriptionException {
        Object loaded;
        try {
            loaded = YAML.load(reader);
        } catch (RuntimeException e) {
            throw new InvalidDescriptionException(e);
        }
        if (!(loaded instanceof Map<?, ?> map)) {
            throw new InvalidDescriptionException("plugin.yml must contain a mapping");
        }
        parse(map);
    }

    private void parse(Map<?, ?> map) throws InvalidDescriptionException {
        name = required(map, "name").replace(' ', '_');
        if (!name.matches("^[A-Za-z0-9 _.-]+$")) {
            throw new InvalidDescriptionException("name '" + name + "' contains invalid characters.");
        }
        version = required(map, "version");
        main = required(map, "main");
        description = text(map.get("description"));
        website = text(map.get("website"));
        prefix = text(map.get("prefix"));
        apiVersion = text(map.get("api-version"));
        Object order = map.get("load");
        if (order != null) {
            try {
                load = PluginLoadOrder.valueOf(String.valueOf(order).toUpperCase(java.util.Locale.ROOT).replace('-', '_'));
            } catch (IllegalArgumentException e) {
                throw new InvalidDescriptionException(e, "load is not a valid choice");
            }
        }
        List<String> people = new ArrayList<>(strings(map.get("authors")));
        if (map.get("author") != null) {
            people.add(String.valueOf(map.get("author")));
        }
        authors = List.copyOf(people);
        depend = strings(map.get("depend"));
        softDepend = strings(map.get("softdepend"));
        loadBefore = strings(map.get("loadbefore"));

        Map<String, Map<String, Object>> parsedCommands = new LinkedHashMap<>();
        if (map.get("commands") instanceof Map<?, ?> declared) {
            declared.forEach((key, value) -> {
                Map<String, Object> attributes = new LinkedHashMap<>();
                if (value instanceof Map<?, ?> values) {
                    values.forEach((attribute, content) -> attributes.put(String.valueOf(attribute), content));
                }
                parsedCommands.put(String.valueOf(key), attributes);
            });
        }
        commands = Collections.unmodifiableMap(parsedCommands);

        Object fallback = map.get("default-permission");
        if (fallback != null) {
            PermissionDefault parsed = PermissionDefault.getByName(String.valueOf(fallback));
            if (parsed != null) {
                defaultPermission = parsed;
            }
        }
        if (map.get("permissions") instanceof Map<?, ?> declared) {
            List<Permission> parsedPermissions = new ArrayList<>();
            declared.forEach((key, value) -> parsedPermissions.add(permission(String.valueOf(key), value)));
            permissions = List.copyOf(parsedPermissions);
        }
    }

    private Permission permission(String name, Object value) {
        String text = null;
        PermissionDefault fallback = defaultPermission;
        Map<String, Boolean> children = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> values) {
            text = text(values.get("description"));
            if (values.get("default") != null) {
                PermissionDefault parsed = PermissionDefault.getByName(String.valueOf(values.get("default")));
                if (parsed != null) {
                    fallback = parsed;
                }
            }
            if (values.get("children") instanceof Map<?, ?> nested) {
                nested.forEach((child, flag) -> children.put(String.valueOf(child), !(flag instanceof Boolean bool) || bool));
            } else if (values.get("children") instanceof List<?> nested) {
                nested.forEach(child -> children.put(String.valueOf(child), true));
            }
        }
        return new Permission(name, text, fallback, children);
    }

    private static String required(Map<?, ?> map, String key) throws InvalidDescriptionException {
        Object value = map.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new InvalidDescriptionException("Missing " + key + " in plugin.yml");
        }
        return String.valueOf(value);
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return value == null ? List.of() : List.of(String.valueOf(value));
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAPIVersion() {
        return apiVersion;
    }

    public PluginLoadOrder getLoad() {
        return load;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public List<String> getDepend() {
        return depend;
    }

    public List<String> getSoftDepend() {
        return softDepend;
    }

    public List<String> getLoadBefore() {
        return loadBefore;
    }

    public Map<String, Map<String, Object>> getCommands() {
        return commands;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public PermissionDefault getPermissionDefault() {
        return defaultPermission;
    }

    public String getFullName() {
        return name + " v" + version;
    }
}
