package org.bukkit.event.entity;

public class EntityShootBowEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.inventory.ItemStack bow;
    private org.bukkit.inventory.ItemStack consumable;
    private org.bukkit.entity.Entity projectile;
    private org.bukkit.inventory.EquipmentSlot hand;
    private float force;
    private boolean consumeItem;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityShootBowEvent(org.bukkit.entity.LivingEntity p0, org.bukkit.inventory.ItemStack p1, org.bukkit.inventory.ItemStack p2, org.bukkit.entity.Entity p3, org.bukkit.inventory.EquipmentSlot p4, float p5, boolean p6) {
        super(p0);
        this.bow = p1;
        this.consumable = p2;
        this.projectile = p3;
        this.hand = p4;
        this.force = p5;
        this.consumeItem = p6;
    }

    public org.bukkit.entity.LivingEntity getEntity() {
        return (org.bukkit.entity.LivingEntity) entity;
    }

    public org.bukkit.inventory.ItemStack getBow() {
        return bow;
    }

    public org.bukkit.inventory.ItemStack getConsumable() {
        return consumable;
    }

    public org.bukkit.entity.Entity getProjectile() {
        return projectile;
    }

    public void setProjectile(org.bukkit.entity.Entity p0) {
        this.projectile = p0;
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        return hand;
    }

    public float getForce() {
        return force;
    }

    public void setConsumeItem(boolean p0) {
        this.consumeItem = p0;
    }

    public boolean shouldConsumeItem() {
        return consumeItem;
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
