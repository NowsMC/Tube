package space.nows.tube.bukkit;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class BukkitPermissible implements Permissible {

    protected final BukkitRuntime runtime;
    private final List<PermissionAttachment> attachments = new CopyOnWriteArrayList<>();

    BukkitPermissible(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    abstract boolean granted(String node);

    @Override
    public boolean isPermissionSet(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        return attachments.stream().anyMatch(attachment -> attachment.getPermissions().containsKey(key)) || granted(name);
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
        if (granted(name)) {
            return true;
        }
        Permission registered = runtime.pluginManager.getPermission(name);
        return registered != null && registered.getDefault().getValue(isOp());
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
        PermissionAttachment attachment = new PermissionAttachment(plugin, this);
        attachments.add(attachment);
        return attachment;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        PermissionAttachment attachment = addAttachment(plugin, name, value);
        runtime.scheduler.runTaskLater(plugin, () -> removeAttachment(attachment), ticks);
        return attachment;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        PermissionAttachment attachment = addAttachment(plugin);
        runtime.scheduler.runTaskLater(plugin, () -> removeAttachment(attachment), ticks);
        return attachment;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        attachments.remove(attachment);
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        Set<PermissionAttachmentInfo> result = new LinkedHashSet<>();
        for (PermissionAttachment attachment : attachments) {
            attachment.getPermissions().forEach((name, value) -> result.add(new PermissionAttachmentInfo(this, name, attachment, value)));
        }
        return result;
    }
}
