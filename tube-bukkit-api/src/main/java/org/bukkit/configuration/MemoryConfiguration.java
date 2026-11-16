package org.bukkit.configuration;

import java.util.Map;

public class MemoryConfiguration extends MemorySection implements Configuration {

    private Configuration defaults;
    private ConfigurationOptions options;

    public MemoryConfiguration() {
    }

    public MemoryConfiguration(Configuration defaults) {
        this.defaults = defaults;
    }

    @Override
    public void addDefault(String path, Object value) {
        if (defaults == null) {
            defaults = new MemoryConfiguration();
        }
        defaults.set(path, value);
    }

    @Override
    public void addDefaults(Map<String, Object> values) {
        values.forEach(this::addDefault);
    }

    @Override
    public void addDefaults(Configuration values) {
        addDefaults(values.getValues(true));
    }

    @Override
    public void setDefaults(Configuration defaults) {
        this.defaults = defaults;
    }

    @Override
    public Configuration getDefaults() {
        return defaults;
    }

    @Override
    public ConfigurationOptions options() {
        if (options == null) {
            options = new ConfigurationOptions(this);
        }
        return options;
    }
}
