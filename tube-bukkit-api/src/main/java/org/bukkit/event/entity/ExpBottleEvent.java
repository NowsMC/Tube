package org.bukkit.event.entity;

public class ExpBottleEvent extends org.bukkit.event.entity.ProjectileHitEvent {
    private int exp;
    private boolean showEffect;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public ExpBottleEvent(org.bukkit.entity.ThrownExpBottle p0, int p1) {
        this(p0, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null, p1);
    }

    public ExpBottleEvent(org.bukkit.entity.ThrownExpBottle p0, org.bukkit.entity.Entity p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, int p4) {
        super(p0, p1, p2, p3);
        this.showEffect = true;
        this.exp = p4;
    }

    public org.bukkit.entity.ThrownExpBottle getEntity() {
        return (org.bukkit.entity.ThrownExpBottle) entity;
    }

    public boolean getShowEffect() {
        return showEffect;
    }

    public void setShowEffect(boolean p0) {
        this.showEffect = p0;
    }

    public int getExperience() {
        return exp;
    }

    public void setExperience(int p0) {
        this.exp = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
