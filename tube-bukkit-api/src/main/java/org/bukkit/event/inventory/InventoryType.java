package org.bukkit.event.inventory;

public enum InventoryType {
    CHEST(27),
    DISPENSER(9),
    DROPPER(9),
    FURNACE(3),
    WORKBENCH(10),
    CRAFTING(5),
    ENCHANTING(2),
    BREWING(5),
    PLAYER(41),
    CREATIVE(9),
    MERCHANT(3),
    ENDER_CHEST(27),
    ANVIL(3),
    SMITHING(4),
    BEACON(1),
    HOPPER(5),
    SHULKER_BOX(27),
    BARREL(27),
    BLAST_FURNACE(3),
    LECTERN(1),
    SMOKER(3),
    LOOM(4),
    CARTOGRAPHY(3),
    GRINDSTONE(3),
    STONECUTTER(2);

    private final int size;

    InventoryType(int size) {
        this.size = size;
    }

    public int getDefaultSize() {
        return size;
    }

    public enum SlotType {
        RESULT,
        CRAFTING,
        ARMOR,
        CONTAINER,
        QUICKBAR,
        OUTSIDE,
        FUEL
    }
}
