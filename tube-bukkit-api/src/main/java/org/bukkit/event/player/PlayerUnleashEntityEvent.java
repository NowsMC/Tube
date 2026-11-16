package org.bukkit.event.player;

public class PlayerUnleashEntityEvent extends org.bukkit.event.entity.EntityUnleashEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.Player player;
    private org.bukkit.inventory.EquipmentSlot hand;

    public PlayerUnleashEntityEvent(org.bukkit.entity.Entity p0, org.bukkit.entity.Player p1, org.bukkit.inventory.EquipmentSlot p2) {
        super(p0, org.bukkit.event.entity.EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH);
        this.cancelled = false;
        this.player = p1;
        this.hand = p2;
    }

    public PlayerUnleashEntityEvent(org.bukkit.entity.Entity p0, org.bukkit.entity.Player p1) {
        this(p0, p1, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        return hand;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }
}
