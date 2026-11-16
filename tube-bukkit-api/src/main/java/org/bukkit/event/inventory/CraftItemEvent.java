package org.bukkit.event.inventory;

public class CraftItemEvent extends org.bukkit.event.inventory.InventoryClickEvent {
    private org.bukkit.inventory.Recipe recipe;

    public CraftItemEvent(org.bukkit.inventory.Recipe p0, org.bukkit.inventory.InventoryView p1, org.bukkit.event.inventory.InventoryType.SlotType p2, int p3, org.bukkit.event.inventory.ClickType p4, org.bukkit.event.inventory.InventoryAction p5) {
        super(p1, p2, p3, p4, p5);
        this.recipe = p0;
    }

    public CraftItemEvent(org.bukkit.inventory.Recipe p0, org.bukkit.inventory.InventoryView p1, org.bukkit.event.inventory.InventoryType.SlotType p2, int p3, org.bukkit.event.inventory.ClickType p4, org.bukkit.event.inventory.InventoryAction p5, int p6) {
        super(p1, p2, p3, p4, p5, p6);
        this.recipe = p0;
    }

    public org.bukkit.inventory.Recipe getRecipe() {
        return recipe;
    }

    public org.bukkit.inventory.CraftingInventory getInventory() {
        throw new UnsupportedOperationException("CraftItemEvent#getInventory is not supported by Tube yet");
    }
}
