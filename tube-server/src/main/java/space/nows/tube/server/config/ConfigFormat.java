package space.nows.tube.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum ConfigFormat {

    YAML("yml", "yaml") {
        @Override
        public Map<String, Object> parse(String text) {
            Object loaded = new Yaml().load(text);
            if (loaded == null) {
                return new LinkedHashMap<>();
            }
            if (loaded instanceof Map<?, ?> map) {
                return FileConfig.copy(map);
            }
            throw new IllegalArgumentException("YAML must contain a mapping at the top level");
        }

        @Override
        String render(Map<String, Object> data) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            return new Yaml(options).dump(data);
        }
    },

    JSON("json") {
        private final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();

        @Override
        public Map<String, Object> parse(String text) {
            Map<String, Object> loaded = gson.fromJson(text, new TypeToken<Map<String, Object>>() { }.getType());
            return loaded == null ? new LinkedHashMap<>() : FileConfig.copy(loaded);
        }

        @Override
        String render(Map<String, Object> data) {
            return gson.toJson(data) + System.lineSeparator();
        }
    },

    TOML("toml") {
        private final JToml toml = JToml.jToml();

        @Override
        public Map<String, Object> parse(String text) {
            return fromTable(toml.readFromString(text));
        }

        @Override
        String render(Map<String, Object> data) {
            return toml.writeToString(toTable(data));
        }
    };

    private final List<String> extensions;

    ConfigFormat(String... extensions) {
        this.extensions = List.of(extensions);
    }

    public abstract Map<String, Object> parse(String text);

    abstract String render(Map<String, Object> data);

    public List<String> extensions() {
        return extensions;
    }

    public static Optional<ConfigFormat> ofFileName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return Optional.empty();
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        for (ConfigFormat format : values()) {
            if (format.extensions.contains(extension)) {
                return Optional.of(format);
            }
        }
        return Optional.empty();
    }

    private static Map<String, Object> fromTable(TomlTable table) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (TomlTable.Entry<?> entry : table.entries(false)) {
            result.put(entry.key().getLast(), fromValue(entry.value()));
        }
        return result;
    }

    private static Object fromValue(TomlValue value) {
        if (value.isTable()) {
            return fromTable(value.asTable());
        }
        if (value.isArray()) {
            return value.asArray().stream().map(ConfigFormat::fromValue).toList();
        }
        TomlPrimitive primitive = value.asPrimitive();
        return switch (primitive.type()) {
            case BOOLEAN -> primitive.asBoolean();
            case INTEGER -> primitive.asLong();
            case FLOAT -> primitive.asDouble();
            default -> primitive.asString();
        };
    }

    private static TomlTable toTable(Map<String, Object> data) {
        TomlTable table = TomlTable.create();
        data.forEach((key, value) -> {
            if (value != null) {
                table.put(TomlKey.literal(key), toValue(value));
            }
        });
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TomlValue toValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return toTable((Map<String, Object>) map);
        }
        if (value instanceof Iterable<?> items) {
            TomlArray array = TomlArray.create();
            items.forEach(item -> array.add(toValue(item)));
            return array;
        }
        if (value instanceof Boolean flag) {
            return TomlPrimitive.of(flag);
        }
        if (value instanceof Double || value instanceof Float) {
            return TomlPrimitive.of(((Number) value).doubleValue());
        }
        if (value instanceof Number number) {
            return TomlPrimitive.of(number.longValue());
        }
        return TomlPrimitive.of(String.valueOf(value));
    }
}
