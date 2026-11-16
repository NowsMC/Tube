package org.bukkit.event.player;

public class PlayerItemConsumeEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private boolean isCancelled;
    private org.bukkit.inventory.ItemStack item;
    private org.bukkit.inventory.EquipmentSlot hand;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerItemConsumeEvent(org.bukkit.entity.Player p0, org.bukkit.inventory.ItemStack p1, org.bukkit.inventory.EquipmentSlot p2) {
        super(p0);
        this.isCancelled = false;
        this.item = p1;
        this.hand = p2;
    }

    public PlayerItemConsumeEvent(org.bukkit.entity.Player p0, org.bukkit.inventory.ItemStack p1) {
        this(p0, p1, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public org.bukkit.inventory.ItemStack getItem() {
        throw new UnsupportedOperationException("PlayerItemConsumeEvent#getItem is not supported by Tube yet");
    }

    public void setItem(org.bukkit.inventory.ItemStack p0) {
        throw new UnsupportedOperationException("PlayerItemConsumeEvent#setItem is not supported by Tube yet");
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        return hand;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean p0) {
        this.isCancelled = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
