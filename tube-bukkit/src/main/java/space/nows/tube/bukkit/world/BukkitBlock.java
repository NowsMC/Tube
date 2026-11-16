package space.nows.tube.bukkit.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import space.nows.tube.bukkit.BukkitRuntime;

import java.util.Objects;

final class BukkitBlock implements Block {

    private final BukkitRuntime runtime;
    private final BukkitWorld world;
    private final int x;
    private final int y;
    private final int z;

    BukkitBlock(BukkitRuntime runtime, BukkitWorld world, int x, int y, int z) {
        this.runtime = runtime;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
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
    public Location getLocation() {
        return new Location(world, x, y, z);
    }

    @Override
    public Material getType() {
        Material material = Material.matchMaterial(world.tube().block(x, y, z).key());
        return material == null ? Material.AIR : material;
    }

    @Override
    public void setType(Material type) {
        world.tube().setBlock(x, y, z, space.nows.tube.api.Block.of(type.getKey().toString()));
    }

    @Override
    public void setType(Material type, boolean applyPhysics) {
        setType(type);
    }

    @Override
    public Block getRelative(BlockFace face) {
        return getRelative(face.getModX(), face.getModY(), face.getModZ());
    }

    @Override
    public Block getRelative(int dx, int dy, int dz) {
        return new BukkitBlock(runtime, world, x + dx, y + dy, z + dz);
    }

    @Override
    public boolean isEmpty() {
        return world.tube().block(x, y, z).isAir();
    }

    @Override
    public org.bukkit.block.data.BlockData getBlockData() {
        return new BukkitBlockData(world.tube().block(x, y, z));
    }

    @Override
    public void setBlockData(org.bukkit.block.data.BlockData data) {
        world.tube().setBlock(x, y, z, ((BukkitBlockData) data).tube());
    }

    @Override
    public void setBlockData(org.bukkit.block.data.BlockData data, boolean applyPhysics) {
        setBlockData(data);
    }

    @Override
    public org.bukkit.block.BlockState getState() {
        return new BukkitBlockState(this);
    }

    @Override
    public org.bukkit.block.BlockState getState(boolean useSnapshot) {
        return getState();
    }

    @Override
    public org.bukkit.Chunk getChunk() {
        return world.getChunkAt(x >> 4, z >> 4);
    }

    @Override
    public boolean isLiquid() {
        Material type = getType();
        return type == Material.WATER || type == Material.LAVA;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BukkitBlock block && block.x == x && block.y == y && block.z == z && block.world.equals(world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, world.tube().name());
    }
}
