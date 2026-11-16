package org.bukkit.inventory.meta;

public interface BlockStateMeta extends org.bukkit.inventory.meta.ItemMeta {

    org.bukkit.block.BlockState getBlockState();

    void setBlockState(org.bukkit.block.BlockState p0);
}
