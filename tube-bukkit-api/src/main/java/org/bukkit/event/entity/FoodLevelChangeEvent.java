package org.bukkit.event.entity;

public class FoodLevelChangeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private int level;
    private org.bukkit.inventory.ItemStack item;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public FoodLevelChangeEvent(org.bukkit.entity.HumanEntity p0, int p1) {
        this(p0, p1, (org.bukkit.inventory.ItemStack) null);
    }

    public FoodLevelChangeEvent(org.bukkit.entity.HumanEntity p0, int p1, org.bukkit.inventory.ItemStack p2) {
        super(p0);
        this.cancel = false;
        this.level = p1;
        this.item = p2;
    }

    public org.bukkit.entity.HumanEntity getEntity() {
        return (org.bukkit.entity.HumanEntity) entity;
    }

    public org.bukkit.inventory.ItemStack getItem() {
        throw new UnsupportedOperationException("FoodLevelChangeEvent#getItem is not supported by Tube yet");
    }

    public int getFoodLevel() {
        return level;
    }

    public void setFoodLevel(int p0) {
        throw new UnsupportedOperationException("FoodLevelChangeEvent#setFoodLevel is not supported by Tube yet");
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
