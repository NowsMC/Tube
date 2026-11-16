package org.bukkit.block;

public interface Jukebox extends org.bukkit.block.TileState, org.bukkit.inventory.BlockInventoryHolder {

    org.bukkit.inventory.ItemStack getRecord();

    void setRecord(org.bukkit.inventory.ItemStack p0);

    boolean isPlaying();
}
