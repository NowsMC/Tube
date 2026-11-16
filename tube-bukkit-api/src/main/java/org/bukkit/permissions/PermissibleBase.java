package org.bukkit.permissions;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class PermissibleBase implements Permissible {

    private final ServerOperator opable;
    private final Permissible parent;
    private final List<PermissionAttachment> attachments = new CopyOnWriteArrayList<>();

    public PermissibleBase(ServerOperator opable) {
        this.opable = opable;
        this.parent = opable instanceof Permissible permissible ? permissible : this;
    }

    @Override
    public boolean isOp() {
        return opable != null && opable.isOp();
    }

    @Override
    public void setOp(boolean value) {
        if (opable == null) {
            throw new UnsupportedOperationException("Cannot change op value as no ServerOperator is set");
        }
        opable.setOp(value);
    }

    @Override
    public boolean isPermissionSet(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        return attachments.stream().anyMatch(attachment -> attachment.getPermissions().containsKey(key));
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return isPermissionSet(perm.getName());
    }

    @Override
    public boolean hasPermission(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        for (int i = attachments.size() - 1; i >= 0; i--) {
            Boolean value = attachments.get(i).getPermissions().get(key);
            if (value != null) {
                return value;
            }
        }
        Permission registered = Bukkit.getServer() == null ? null : Bukkit.getServer().getPluginManager().getPermission(key);
        return registered != null ? registered.getDefault().getValue(isOp()) : Permission.DEFAULT_PERMISSION.getValue(isOp());
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return hasPermission(perm.getName());
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        PermissionAttachment attachment = addAttachment(plugin);
        attachment.setPermission(name, value);
        return attachment;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        if (plugin == null || !plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin " + (plugin == null ? "null" : plugin.getName()) + " is disabled");
        }
        PermissionAttachment attachment = new PermissionAttachment(plugin, parent);
        attachments.add(attachment);
        return attachment;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        PermissionAttachment attachment = addAttachment(plugin, ticks);
        attachment.setPermission(name, value);
        return attachment;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        PermissionAttachment attachment = addAttachment(plugin);
        Bukkit.getScheduler().runTaskLater(plugin, () -> removeAttachment(attachment), ticks);
        return attachment;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        if (!attachments.remove(attachment)) {
            throw new IllegalArgumentException("Given attachment is not part of Permissible object " + parent);
        }
    }

    @Override
    public void recalculatePermissions() {
    }

    public void clearPermissions() {
        attachments.clear();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        Set<PermissionAttachmentInfo> result = new LinkedHashSet<>();
        for (PermissionAttachment attachment : attachments) {
            attachment.getPermissions().forEach((name, value) -> result.add(new PermissionAttachmentInfo(parent, name, attachment, value)));
        }
        return result;
    }
}
