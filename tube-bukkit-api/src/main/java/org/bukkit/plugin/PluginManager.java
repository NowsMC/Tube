package org.bukkit.plugin;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

import java.util.Set;

public interface PluginManager {

    Plugin getPlugin(String name);

    Plugin[] getPlugins();

    boolean isPluginEnabled(String name);

    boolean isPluginEnabled(Plugin plugin);

    void disablePlugins();

    void enablePlugin(Plugin plugin);

    void disablePlugin(Plugin plugin);

    void callEvent(Event event);

    void registerEvents(Listener listener, Plugin plugin);

    void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin);

    void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin, boolean ignoreCancelled);

    Permission getPermission(String name);

    void addPermission(Permission permission);

    void removePermission(Permission permission);

    void removePermission(String name);

    Set<Permission> getDefaultPermissions(boolean op);

    Set<Permission> getPermissions();
}
