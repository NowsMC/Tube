package org.bukkit.event.hanging;

public class HangingPlaceEvent extends org.bukkit.event.hanging.HangingEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.Player player;
    private org.bukkit.block.Block block;
    private org.bukkit.block.BlockFace blockFace;
    private org.bukkit.inventory.EquipmentSlot hand;
    private org.bukkit.inventory.ItemStack itemStack;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public HangingPlaceEvent(org.bukkit.entity.Hanging p0, org.bukkit.entity.Player p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, org.bukkit.inventory.EquipmentSlot p4) {
        this(p0, p1, p2, p3, p4, (org.bukkit.inventory.ItemStack) null);
    }

    public HangingPlaceEvent(org.bukkit.entity.Hanging p0, org.bukkit.entity.Player p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, org.bukkit.inventory.EquipmentSlot p4, org.bukkit.inventory.ItemStack p5) {
        super(p0);
        this.player = p1;
        this.block = p2;
        this.blockFace = p3;
        this.hand = p4;
        this.itemStack = p5;
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public org.bukkit.block.Block getBlock() {
        return block;
    }

    public org.bukkit.block.BlockFace getBlockFace() {
        return blockFace;
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        return hand;
    }

    public org.bukkit.inventory.ItemStack getItemStack() {
        return itemStack;
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
