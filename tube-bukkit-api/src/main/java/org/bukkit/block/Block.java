package org.bukkit.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public interface Block {

    int getX();

    int getY();

    int getZ();

    World getWorld();

    Location getLocation();

    Material getType();

    void setType(Material type);

    void setType(Material type, boolean applyPhysics);

    Block getRelative(BlockFace face);

    Block getRelative(int dx, int dy, int dz);

    boolean isEmpty();

    boolean isLiquid();

    org.bukkit.block.data.BlockData getBlockData();

    void setBlockData(org.bukkit.block.data.BlockData data);

    void setBlockData(org.bukkit.block.data.BlockData data, boolean applyPhysics);

    BlockState getState();

    BlockState getState(boolean useSnapshot);

    org.bukkit.Chunk getChunk();
}
