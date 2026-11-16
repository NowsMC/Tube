package org.bukkit.event.player;

public class PlayerShearEntityEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private org.bukkit.entity.Entity what;
    private org.bukkit.inventory.ItemStack item;
    private org.bukkit.inventory.EquipmentSlot hand;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerShearEntityEvent(org.bukkit.entity.Player p0, org.bukkit.entity.Entity p1, org.bukkit.inventory.ItemStack p2, org.bukkit.inventory.EquipmentSlot p3) {
        super(p0);
        this.what = p1;
        this.item = p2;
        this.hand = p3;
    }

    public PlayerShearEntityEvent(org.bukkit.entity.Player p0, org.bukkit.entity.Entity p1) {
        this(p0, p1, new org.bukkit.inventory.ItemStack(org.bukkit.Material.SHEARS), org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public org.bukkit.entity.Entity getEntity() {
        return what;
    }

    public org.bukkit.inventory.ItemStack getItem() {
        return item == null ? null : item.clone();
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
