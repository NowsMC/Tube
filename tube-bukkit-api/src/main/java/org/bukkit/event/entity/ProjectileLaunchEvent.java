package org.bukkit.event.entity;

public class ProjectileLaunchEvent extends org.bukkit.event.entity.EntitySpawnEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;

    public ProjectileLaunchEvent(org.bukkit.entity.Entity p0) {
        super(p0);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public org.bukkit.entity.Projectile getEntity() {
        return (org.bukkit.entity.Projectile) entity;
    }
}
