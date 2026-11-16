package org.bukkit.event.server;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public abstract class PluginEvent extends Event {

    private final Plugin plugin;

    public PluginEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
