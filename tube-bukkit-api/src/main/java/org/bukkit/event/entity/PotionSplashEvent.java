package org.bukkit.event.entity;

public class PotionSplashEvent extends org.bukkit.event.entity.ProjectileHitEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private java.util.Map<org.bukkit.entity.LivingEntity, java.lang.Double> affectedEntities;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PotionSplashEvent(org.bukkit.entity.ThrownPotion p0, java.util.Map<org.bukkit.entity.LivingEntity, java.lang.Double> p1) {
        this(p0, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null, p1);
    }

    public PotionSplashEvent(org.bukkit.entity.ThrownPotion p0, org.bukkit.entity.Entity p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, java.util.Map<org.bukkit.entity.LivingEntity, java.lang.Double> p4) {
        super(p0, p1, p2, p3);
        this.affectedEntities = p4;
    }

    public org.bukkit.entity.ThrownPotion getEntity() {
        return (org.bukkit.entity.ThrownPotion) entity;
    }

    public org.bukkit.entity.ThrownPotion getPotion() {
        throw new UnsupportedOperationException("PotionSplashEvent#getPotion is not supported by Tube yet");
    }

    public java.util.Collection<org.bukkit.entity.LivingEntity> getAffectedEntities() {
        throw new UnsupportedOperationException("PotionSplashEvent#getAffectedEntities is not supported by Tube yet");
    }

    public double getIntensity(org.bukkit.entity.LivingEntity p0) {
        throw new UnsupportedOperationException("PotionSplashEvent#getIntensity is not supported by Tube yet");
    }

    public void setIntensity(org.bukkit.entity.LivingEntity p0, double p1) {
        throw new UnsupportedOperationException("PotionSplashEvent#setIntensity is not supported by Tube yet");
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
