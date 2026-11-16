package org.bukkit.event.entity;

public class EntitySpawnEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean canceled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntitySpawnEvent(org.bukkit.entity.Entity p0) {
        super(p0);
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(boolean p0) {
        this.canceled = p0;
    }

    public org.bukkit.Location getLocation() {
        throw new UnsupportedOperationException("EntitySpawnEvent#getLocation is not supported by Tube yet");
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
