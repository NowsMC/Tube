package org.bukkit.entity;

public interface FallingBlock extends org.bukkit.entity.Entity {

    org.bukkit.block.data.BlockData getBlockData();

    boolean getDropItem();
}
