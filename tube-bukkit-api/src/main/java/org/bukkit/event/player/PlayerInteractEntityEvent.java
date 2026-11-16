package org.bukkit.event.player;

public class PlayerInteractEntityEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    protected org.bukkit.entity.Entity clickedEntity;
    private boolean cancelled;
    private org.bukkit.inventory.EquipmentSlot hand;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerInteractEntityEvent(org.bukkit.entity.Player p0, org.bukkit.entity.Entity p1) {
        this(p0, p1, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public PlayerInteractEntityEvent(org.bukkit.entity.Player p0, org.bukkit.entity.Entity p1, org.bukkit.inventory.EquipmentSlot p2) {
        super(p0);
        this.cancelled = false;
        this.clickedEntity = p1;
        this.hand = p2;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public org.bukkit.entity.Entity getRightClicked() {
        return clickedEntity;
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        return hand;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
