package org.bukkit.configuration;

import java.util.Map;

public interface Configuration extends ConfigurationSection {

    void addDefaults(Map<String, Object> defaults);

    void addDefaults(Configuration defaults);

    void setDefaults(Configuration defaults);

    Configuration getDefaults();

    ConfigurationOptions options();
}
