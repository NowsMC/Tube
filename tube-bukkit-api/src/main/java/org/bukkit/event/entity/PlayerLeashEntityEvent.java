package org.bukkit.event.entity;

public class PlayerLeashEntityEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    private org.bukkit.entity.Entity leashHolder;
    private org.bukkit.entity.Entity entity;
    private boolean cancelled;
    private org.bukkit.entity.Player player;
    private org.bukkit.inventory.EquipmentSlot hand;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerLeashEntityEvent(org.bukkit.entity.Entity p0, org.bukkit.entity.Entity p1, org.bukkit.entity.Player p2, org.bukkit.inventory.EquipmentSlot p3) {
        super();
        this.cancelled = false;
        this.leashHolder = p1;
        this.entity = p0;
        this.player = p2;
        this.hand = p3;
    }

    public PlayerLeashEntityEvent(org.bukkit.entity.Entity p0, org.bukkit.entity.Entity p1, org.bukkit.entity.Player p2) {
        this(p0, p1, p2, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public org.bukkit.entity.Entity getLeashHolder() {
        return leashHolder;
    }

    public org.bukkit.entity.Entity getEntity() {
        return entity;
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }
}
