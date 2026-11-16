package org.bukkit.plugin;

public abstract class PluginBase implements Plugin {

    @Override
    public final int hashCode() {
        return getName().hashCode();
    }

    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof Plugin that && getName().equals(that.getName());
    }

    @Override
    public final String getName() {
        return getDescription().getName();
    }
}
