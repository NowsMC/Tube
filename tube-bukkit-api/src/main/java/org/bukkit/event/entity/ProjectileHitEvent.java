package org.bukkit.event.entity;

public class ProjectileHitEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.entity.Entity hitEntity;
    private org.bukkit.block.Block hitBlock;
    private org.bukkit.block.BlockFace hitFace;
    private boolean cancel;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public ProjectileHitEvent(org.bukkit.entity.Projectile p0) {
        this(p0, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null);
    }

    public ProjectileHitEvent(org.bukkit.entity.Projectile p0, org.bukkit.entity.Entity p1) {
        this(p0, p1, (org.bukkit.block.Block) null);
    }

    public ProjectileHitEvent(org.bukkit.entity.Projectile p0, org.bukkit.block.Block p1) {
        this(p0, (org.bukkit.entity.Entity) null, p1);
    }

    public ProjectileHitEvent(org.bukkit.entity.Projectile p0, org.bukkit.entity.Entity p1, org.bukkit.block.Block p2) {
        this(p0, p1, p2, (org.bukkit.block.BlockFace) null);
    }

    public ProjectileHitEvent(org.bukkit.entity.Projectile p0, org.bukkit.entity.Entity p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3) {
        super(p0);
        this.cancel = false;
        this.hitEntity = p1;
        this.hitBlock = p2;
        this.hitFace = p3;
    }

    public org.bukkit.entity.Projectile getEntity() {
        return (org.bukkit.entity.Projectile) entity;
    }

    public org.bukkit.block.Block getHitBlock() {
        return hitBlock;
    }

    public org.bukkit.block.BlockFace getHitBlockFace() {
        return hitFace;
    }

    public org.bukkit.entity.Entity getHitEntity() {
        return hitEntity;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
