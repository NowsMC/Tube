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

    default BlockFace getFace(Block block) {
        for (BlockFace face : BlockFace.values()) {
            if (getX() + face.getModX() == block.getX() && getY() + face.getModY() == block.getY() && getZ() + face.getModZ() == block.getZ()
                    && java.util.Objects.equals(getWorld(), block.getWorld())) {
                return face;
            }
        }
        return null;
    }

    default byte getData() {
        throw new UnsupportedOperationException("Block#getData uses legacy ids, which Tube does not have");
    }

    default int getTypeId() {
        throw new UnsupportedOperationException("Block#getTypeId uses legacy ids, which Tube does not have");
    }

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
