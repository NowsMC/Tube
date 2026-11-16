package org.bukkit.event.block;

public class BlockDamageEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.entity.Player player;
    private boolean instaBreak;
    private boolean cancel;
    private org.bukkit.inventory.ItemStack itemstack;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockDamageEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1, org.bukkit.inventory.ItemStack p2, boolean p3) {
        super(p1);
        this.instaBreak = p3;
        this.player = p0;
        this.itemstack = p2;
        this.cancel = false;
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public boolean getInstaBreak() {
        return instaBreak;
    }

    public void setInstaBreak(boolean p0) {
        this.instaBreak = p0;
    }

    public org.bukkit.inventory.ItemStack getItemInHand() {
        return itemstack;
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
