package org.bukkit.event.inventory;

public class InventoryCreativeEvent extends org.bukkit.event.inventory.InventoryClickEvent {
    private org.bukkit.inventory.ItemStack item;

    public InventoryCreativeEvent(org.bukkit.inventory.InventoryView p0, org.bukkit.event.inventory.InventoryType.SlotType p1, int p2, org.bukkit.inventory.ItemStack p3) {
        super(p0, p1, p2, org.bukkit.event.inventory.ClickType.CREATIVE, org.bukkit.event.inventory.InventoryAction.PLACE_ALL);
        this.item = p3;
    }

    public org.bukkit.inventory.ItemStack getCursor() {
        return item;
    }

    public void setCursor(org.bukkit.inventory.ItemStack p0) {
        this.item = p0;
    }
}
