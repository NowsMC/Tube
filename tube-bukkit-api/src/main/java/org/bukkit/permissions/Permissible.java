package org.bukkit.permissions;

import org.bukkit.plugin.Plugin;

import java.util.Set;

public interface Permissible extends ServerOperator {

    boolean isPermissionSet(String name);

    boolean isPermissionSet(Permission perm);

    boolean hasPermission(String name);

    boolean hasPermission(Permission perm);

    PermissionAttachment addAttachment(Plugin plugin, String name, boolean value);

    PermissionAttachment addAttachment(Plugin plugin);

    PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks);

    PermissionAttachment addAttachment(Plugin plugin, int ticks);

    void removeAttachment(PermissionAttachment attachment);

    void recalculatePermissions();

    Set<PermissionAttachmentInfo> getEffectivePermissions();
}
