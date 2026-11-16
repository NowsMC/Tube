package space.nows.tube.server.config;

import space.nows.tube.api.PluginConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FileConfig implements PluginConfig {

    private final Path file;
    private final ConfigFormat format;
    private Map<String, Object> root = new LinkedHashMap<>();

    private FileConfig(Path file, ConfigFormat format) {
        this.file = file;
        this.format = format;
    }

    public static FileConfig load(Path file) {
        ConfigFormat format = ConfigFormat.ofFileName(file.getFileName().toString())
                .orElseThrow(() -> new IllegalArgumentException("Unsupported config format: " + file.getFileName()));
        FileConfig config = new FileConfig(file, format);
        config.reload();
        return config;
    }

    @Override
    public void reload() {
        if (!Files.isRegularFile(file)) {
            root = new LinkedHashMap<>();
            return;
        }
        try {
            root = format.parse(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not parse " + file + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void save() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, format.render(root), StandardCharsets.UTF_8);
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

    static Map<String, Object> copy(Map<?, ?> source) {
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
