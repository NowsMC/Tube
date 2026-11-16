package space.nows.tube.server;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import space.nows.tube.api.PluginConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlConfig implements PluginConfig {

    private final Path file;
    private Map<String, Object> root = new LinkedHashMap<>();

    private YamlConfig(Path file) {
        this.file = file;
    }

    public static YamlConfig load(Path file) {
        YamlConfig config = new YamlConfig(file);
        config.reload();
        return config;
    }

    @Override
    public void reload() {
        if (!Files.isRegularFile(file)) {
            root = new LinkedHashMap<>();
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            if (loaded == null) {
                root = new LinkedHashMap<>();
            } else if (loaded instanceof Map<?, ?> map) {
                root = copy(map);
            } else {
                throw new IllegalStateException(file + " must contain a mapping at the top level");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void save() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                new Yaml(options).dump(root, writer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean contains(String path) {
        return find(path) != null;
    }

    @Override
    public String getString(String path, String fallback) {
        Object value = find(path);
        return value == null ? fallback : String.valueOf(value);
    }

    @Override
    public int getInt(String path, int fallback) {
        return find(path) instanceof Number number ? number.intValue() : fallback;
    }

    @Override
    public double getDouble(String path, double fallback) {
        return find(path) instanceof Number number ? number.doubleValue() : fallback;
    }

    @Override
    public boolean getBoolean(String path, boolean fallback) {
        return find(path) instanceof Boolean value ? value : fallback;
    }

    @Override
    public List<String> getStringList(String path) {
        if (find(path) instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> node = root;
        for (int i = 0; i < keys.length - 1; i++) {
            Object child = node.get(keys[i]);
            if (child instanceof Map<?, ?>) {
                node = (Map<String, Object>) child;
            } else {
                Map<String, Object> created = new LinkedHashMap<>();
                node.put(keys[i], created);
                node = created;
            }
        }
        String last = keys[keys.length - 1];
        if (value == null) {
            node.remove(last);
        } else {
            node.put(last, value);
        }
    }

    private Object find(String path) {
        Object node = root;
        for (String key : path.split("\\.")) {
            if (!(node instanceof Map<?, ?> map)) {
                return null;
            }
            node = map.get(key);
        }
        return node;
    }

    private static Map<String, Object> copy(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), copyValue(value)));
        return result;
    }

    private static Object copyValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return copy(map);
        }
        if (value instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        return value;
    }
}
