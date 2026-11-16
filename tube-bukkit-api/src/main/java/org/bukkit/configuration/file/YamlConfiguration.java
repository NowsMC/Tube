package org.bukkit.configuration.file;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

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
            map.forEach((key, value) -> set(String.valueOf(key), value));
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
        String header = options().header();
        String body = data.isEmpty() ? "" : new Yaml(dumper).dump(data);
        return header == null || header.isEmpty() ? body : header.lines().map(line -> "# " + line).collect(java.util.stream.Collectors.joining("\n")) + "\n" + body;
    }

    private static Map<String, Object> toMap(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            result.put(key, value instanceof ConfigurationSection nested ? toMap(nested) : value);
        }
        return result;
    }

    @Override
    public YamlConfigurationOptions options() {
        if (options == null) {
            options = new YamlConfigurationOptions(this);
        }
        return options;
    }
}
