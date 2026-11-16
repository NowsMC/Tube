package org.bukkit.configuration.file;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlConfiguration extends FileConfiguration {

    private YamlConfigurationOptions options;

    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (java.io.FileNotFoundException | java.nio.file.NoSuchFileException ignored) {
            // a missing file is an empty configuration
        } catch (IOException | InvalidConfigurationException e) {
            java.util.logging.Logger.getLogger("Minecraft").log(java.util.logging.Level.SEVERE, "Cannot load " + file, e);
        }
        return configuration;
    }

    public static YamlConfiguration loadConfiguration(Reader reader) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(reader);
        } catch (IOException | InvalidConfigurationException e) {
            java.util.logging.Logger.getLogger("Minecraft").log(java.util.logging.Level.SEVERE, "Cannot load configuration from stream", e);
        }
        return configuration;
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        Object loaded;
        try {
            loaded = new Yaml().load(contents);
        } catch (RuntimeException e) {
            throw new InvalidConfigurationException(e);
        }
        if (loaded != null && !(loaded instanceof Map<?, ?>)) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }
        for (String key : getKeys(false)) {
            set(key, null);
        }
        if (loaded instanceof Map<?, ?> map) {
            map.forEach((key, value) -> set(String.valueOf(key), fromYaml(value)));
        }
    }

    @Override
    public String saveToString() {
        DumperOptions dumper = new DumperOptions();
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setIndent(options().indent());
        dumper.setWidth(options().width());
        Map<String, Object> data = new LinkedHashMap<>();
        Configuration defaults = getDefaults();
        if (options().copyDefaults() && defaults instanceof YamlConfiguration yamlDefaults) {
            data.putAll(toMap(yamlDefaults));
        }
        data.putAll(toMap(this));
        String body = data.isEmpty() ? "" : new Yaml(dumper).dump(data);
        String header = comment(options().getHeader());
        String footer = comment(options().getFooter());
        return (header.isEmpty() ? "" : header + "\n") + body + (footer.isEmpty() ? "" : footer + "\n");
    }

    private static String comment(List<String> lines) {
        return lines.stream().map(line -> line.isEmpty() ? "#" : "# " + line).collect(Collectors.joining("\n"));
    }

    private static Map<String, Object> toMap(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            result.put(key, toYaml(section.get(key)));
        }
        return result;
    }

    private static Object fromYaml(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> converted = new LinkedHashMap<>();
            map.forEach((key, nested) -> converted.put(String.valueOf(key), fromYaml(nested)));
            if (converted.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                ConfigurationSerializable object = ConfigurationSerialization.deserializeObject(converted);
                return object != null ? object : converted;
            }
            return converted;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(YamlConfiguration::fromYaml).collect(Collectors.toCollection(ArrayList::new));
        }
        return value;
    }

    private static Object toYaml(Object value) {
        if (value instanceof ConfigurationSection section) {
            return toMap(section);
        }
        if (value instanceof ConfigurationSerializable serializable) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            serializable.serialize().forEach((key, nested) -> result.put(key, toYaml(nested)));
            return result;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, nested) -> result.put(String.valueOf(key), toYaml(nested)));
            return result;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(YamlConfiguration::toYaml).collect(Collectors.toCollection(ArrayList::new));
        }
        return value;
    }

    @Override
    public YamlConfigurationOptions options() {
        if (options == null) {
            options = new YamlConfigurationOptions(this);
        }
        return options;
    }
}
