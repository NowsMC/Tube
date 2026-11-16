package org.bukkit.permissions;

import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class PermissionAttachment {

    private final Plugin plugin;
    private final Permissible permissible;
    private final Map<String, Boolean> permissions = new LinkedHashMap<>();

    public PermissionAttachment(Plugin plugin, Permissible permissible) {
        this.plugin = plugin;
        this.permissible = permissible;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Permissible getPermissible() {
        return permissible;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermission(String name, boolean value) {
        permissions.put(name.toLowerCase(java.util.Locale.ROOT), value);
        permissible.recalculatePermissions();
    }

    public void unsetPermission(String name) {
        permissions.remove(name.toLowerCase(java.util.Locale.ROOT));
        permissible.recalculatePermissions();
    }

    public boolean remove() {
        permissible.removeAttachment(this);
        return true;
    }
}
