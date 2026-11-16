package org.bukkit.block;

public interface ChiseledBookshelf extends org.bukkit.block.TileState, org.bukkit.inventory.BlockInventoryHolder {

    org.bukkit.inventory.ChiseledBookshelfInventory getInventory();

    int getSlot(org.bukkit.util.Vector p0);
}
