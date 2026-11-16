package space.nows.tube.bukkit.world;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import space.nows.tube.bukkit.BukkitRuntime;

final class BukkitChunk implements org.bukkit.Chunk {

    private final BukkitRuntime runtime;
    private final BukkitWorld world;
    private final int x;
    private final int z;

    BukkitChunk(BukkitRuntime runtime, BukkitWorld world, int x, int z) {
        this.runtime = runtime;
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Block getBlock(int blockX, int y, int blockZ) {
        return world.getBlockAt((x << 4) + (blockX & 15), y, (z << 4) + (blockZ & 15));
    }

    @Override
    public Entity[] getEntities() {
        return world.tube().entities().stream()
                .filter(entity -> ((int) Math.floor(entity.location().x()) >> 4) == x && ((int) Math.floor(entity.location().z()) >> 4) == z)
                .filter(entity -> entity instanceof space.nows.tube.api.LivingEntity)
                .map(entity -> (Entity) runtime.adapters().living((space.nows.tube.api.LivingEntity) entity))
                .toArray(Entity[]::new);
    }

    @Override
    public org.bukkit.block.BlockState[] getTileEntities() {
        return new org.bukkit.block.BlockState[0];
    }

    @Override
    public org.bukkit.block.BlockState[] getTileEntities(boolean useSnapshot) {
        return new org.bukkit.block.BlockState[0];
    }

    @Override
    public boolean isLoaded() {
        return world.tube().isChunkLoaded(x, z);
    }

    @Override
    public boolean load() {
        world.tube().loadChunk(x, z).join();
        return true;
    }

    @Override
    public boolean load(boolean generate) {
        return load();
    }

    @Override
    public boolean unload() {
        world.tube().unloadChunk(x, z);
        return true;
    }

    @Override
    public boolean isGenerated() {
        return true;
    }

    @Override
    public boolean isLocalChunk() {
        return true;
    }

    @Override
    public boolean isExternalChunk() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BukkitChunk chunk && chunk.x == x && chunk.z == z && chunk.world.equals(world);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, z, world.tube().name());
    }

    @Override
    public String toString() {
        return "CraftChunk{x=" + x + ",z=" + z + "}";
    }
}
