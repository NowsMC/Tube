package org.bukkit.event.entity;

public class EntityTameEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.AnimalTamer owner;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityTameEvent(org.bukkit.entity.LivingEntity p0, org.bukkit.entity.AnimalTamer p1) {
        super(p0);
        this.owner = p1;
    }

    public org.bukkit.entity.LivingEntity getEntity() {
        return (org.bukkit.entity.LivingEntity) entity;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public org.bukkit.entity.AnimalTamer getOwner() {
        return owner;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
