package org.bukkit.event.block;

public class CampfireStartEvent extends org.bukkit.event.block.InventoryBlockStartEvent {
    private int cookingTime;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public CampfireStartEvent(org.bukkit.block.Block p0, org.bukkit.inventory.ItemStack p1, int p2) {
        super(p0, p1);
        this.cookingTime = p2;
    }

    public int getTotalCookTime() {
        return cookingTime;
    }

    public void setTotalCookTime(int p0) {
        this.cookingTime = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
