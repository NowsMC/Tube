package org.bukkit.event.player;

public class PlayerArmorStandManipulateEvent extends org.bukkit.event.player.PlayerInteractEntityEvent {
    private org.bukkit.inventory.ItemStack playerItem;
    private org.bukkit.inventory.ItemStack armorStandItem;
    private org.bukkit.inventory.EquipmentSlot slot;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerArmorStandManipulateEvent(org.bukkit.entity.Player p0, org.bukkit.entity.ArmorStand p1, org.bukkit.inventory.ItemStack p2, org.bukkit.inventory.ItemStack p3, org.bukkit.inventory.EquipmentSlot p4, org.bukkit.inventory.EquipmentSlot p5) {
        super(p0, p1, p5);
        this.playerItem = p2;
        this.armorStandItem = p3;
        this.slot = p4;
    }

    public PlayerArmorStandManipulateEvent(org.bukkit.entity.Player p0, org.bukkit.entity.ArmorStand p1, org.bukkit.inventory.ItemStack p2, org.bukkit.inventory.ItemStack p3, org.bukkit.inventory.EquipmentSlot p4) {
        this(p0, p1, p2, p3, p4, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public org.bukkit.inventory.ItemStack getPlayerItem() {
        return playerItem;
    }

    public org.bukkit.inventory.ItemStack getArmorStandItem() {
        return armorStandItem;
    }

    public org.bukkit.inventory.EquipmentSlot getSlot() {
        return slot;
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        throw new UnsupportedOperationException("PlayerArmorStandManipulateEvent#getHand is not supported by Tube yet");
    }

    public org.bukkit.entity.ArmorStand getRightClicked() {
        return (org.bukkit.entity.ArmorStand) clickedEntity;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
