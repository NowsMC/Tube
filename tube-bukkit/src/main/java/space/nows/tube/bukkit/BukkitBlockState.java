package space.nows.tube.bukkit;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

final class BukkitBlockState implements BlockState {

    private final Block block;
    private BlockData data;

    BukkitBlockState(Block block) {
        this.block = block;
        this.data = block.getBlockData();
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockData getBlockData() {
        return data.clone();
    }

    @Override
    public void setBlockData(BlockData data) {
        this.data = data.clone();
    }

    @Override
    public Material getType() {
        return data.getMaterial();
    }

    @Override
    public void setType(Material type) {
        this.data = new BukkitBlockData(space.nows.tube.api.Block.of(type.getKey().toString()));
    }

    @Override
    public World getWorld() {
        return block.getWorld();
    }

    @Override
    public int getX() {
        return block.getX();
    }

    @Override
    public int getY() {
        return block.getY();
    }

    @Override
    public int getZ() {
        return block.getZ();
    }

    @Override
    public Location getLocation() {
        return block.getLocation();
    }

    @Override
    public Chunk getChunk() {
        return block.getChunk();
    }

    @Override
    public boolean isPlaced() {
        return true;
    }

    @Override
    public boolean update() {
        return update(false, true);
    }

    @Override
    public boolean update(boolean force) {
        return update(force, true);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        if (!force && !block.getBlockData().matches(data) && block.getType() != data.getMaterial()) {
            return false;
        }
        block.setBlockData(data, applyPhysics);
        return true;
    }
}
