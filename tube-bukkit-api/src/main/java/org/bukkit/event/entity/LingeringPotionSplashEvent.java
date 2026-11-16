package org.bukkit.event.entity;

public class LingeringPotionSplashEvent extends org.bukkit.event.entity.ProjectileHitEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.AreaEffectCloud entity;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public LingeringPotionSplashEvent(org.bukkit.entity.ThrownPotion p0, org.bukkit.entity.AreaEffectCloud p1) {
        this(p0, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null, p1);
    }

    public LingeringPotionSplashEvent(org.bukkit.entity.ThrownPotion p0, org.bukkit.entity.Entity p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, org.bukkit.entity.AreaEffectCloud p4) {
        super(p0, p1, p2, p3);
        this.entity = p4;
    }

    public org.bukkit.entity.ThrownPotion getEntity() {
        throw new UnsupportedOperationException("LingeringPotionSplashEvent#getEntity is not supported by Tube yet");
    }

    public org.bukkit.entity.AreaEffectCloud getAreaEffectCloud() {
        return entity;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
