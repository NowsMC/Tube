package org.bukkit.block;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

public interface BlockState {

    Block getBlock();

    BlockData getBlockData();

    void setBlockData(BlockData data);

    Material getType();

    void setType(Material type);

    World getWorld();

    int getX();

    int getY();

    int getZ();

    Location getLocation();

    Chunk getChunk();

    boolean isPlaced();

    boolean update();

    boolean update(boolean force);

    boolean update(boolean force, boolean applyPhysics);
}
