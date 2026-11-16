package org.bukkit;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

public interface Chunk {

    int getX();

    int getZ();

    World getWorld();

    Block getBlock(int x, int y, int z);

    Entity[] getEntities();

    BlockState[] getTileEntities();

    BlockState[] getTileEntities(boolean useSnapshot);

    boolean isLoaded();

    boolean load();

    boolean load(boolean generate);

    boolean unload();

    boolean isGenerated();

    boolean isLocalChunk();

    boolean isExternalChunk();
}
