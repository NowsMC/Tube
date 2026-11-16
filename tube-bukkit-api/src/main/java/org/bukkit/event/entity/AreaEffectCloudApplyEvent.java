package org.bukkit.event.entity;

public class AreaEffectCloudApplyEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private java.util.List<org.bukkit.entity.LivingEntity> affectedEntities;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public AreaEffectCloudApplyEvent(org.bukkit.entity.AreaEffectCloud p0, java.util.List<org.bukkit.entity.LivingEntity> p1) {
        super(p0);
        this.cancelled = false;
        this.affectedEntities = p1;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public org.bukkit.entity.AreaEffectCloud getEntity() {
        return (org.bukkit.entity.AreaEffectCloud) entity;
    }

    public java.util.List<org.bukkit.entity.LivingEntity> getAffectedEntities() {
        return affectedEntities;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
