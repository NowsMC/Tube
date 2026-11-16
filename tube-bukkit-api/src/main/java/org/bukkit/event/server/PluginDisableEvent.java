package org.bukkit.event.server;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class PluginDisableEvent extends PluginEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public PluginDisableEvent(Plugin plugin) {
        super(plugin);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
