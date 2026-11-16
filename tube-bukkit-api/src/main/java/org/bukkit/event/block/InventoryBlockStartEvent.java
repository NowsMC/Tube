package org.bukkit.event.block;

public class InventoryBlockStartEvent extends org.bukkit.event.block.BlockEvent {
    private org.bukkit.inventory.ItemStack source;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public InventoryBlockStartEvent(org.bukkit.block.Block p0, org.bukkit.inventory.ItemStack p1) {
        super(p0);
        this.source = p1;
    }

    public org.bukkit.inventory.ItemStack getSource() {
        return source;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
