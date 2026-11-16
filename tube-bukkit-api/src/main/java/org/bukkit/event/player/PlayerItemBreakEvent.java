package org.bukkit.event.player;

public class PlayerItemBreakEvent extends org.bukkit.event.player.PlayerEvent {
    private org.bukkit.inventory.ItemStack brokenItem;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerItemBreakEvent(org.bukkit.entity.Player p0, org.bukkit.inventory.ItemStack p1) {
        super(p0);
        this.brokenItem = p1;
    }

    public org.bukkit.inventory.ItemStack getBrokenItem() {
        return brokenItem;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
