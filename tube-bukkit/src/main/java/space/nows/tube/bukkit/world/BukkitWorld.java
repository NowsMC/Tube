package space.nows.tube.bukkit.world;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import space.nows.tube.bukkit.BukkitRuntime;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public final class BukkitWorld implements World {

    private final BukkitRuntime runtime;
    private final space.nows.tube.api.world.World tube;

    public BukkitWorld(BukkitRuntime runtime, space.nows.tube.api.world.World tube) {
        this.runtime = runtime;
        this.tube = tube;
    }

    public space.nows.tube.api.world.World tube() {
        return tube;
    }

    @Override
    public String getName() {
        return tube.name();
    }

    @Override
    public java.io.File getWorldFolder() {
        return tube.folder().toFile();
    }

    @Override
    public UUID getUID() {
        return UUID.nameUUIDFromBytes(("tube:world:" + tube.name()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public NamespacedKey getKey() {
        return NamespacedKey.minecraft(tube.name().toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public Location getSpawnLocation() {
        return runtime.adapters().location(tube.spawn());
    }

    @Override
    public Environment getEnvironment() {
        return Environment.NORMAL;
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return new BukkitBlock(runtime, this, x, y, z);
    }

    @Override
    public Block getBlockAt(Location location) {
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public List<Player> getPlayers() {
        return runtime.tube().players().stream()
                .filter(player -> player.world().name().equals(tube.name()))
                .map(runtime.adapters()::player)
                .toList();
    }

    @Override
    public List<Entity> getEntities() {
        return List.copyOf(getPlayers());
    }

    @Override
    public int getMinHeight() {
        return -64;
    }

    @Override
    public int getMaxHeight() {
        return 320;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public org.bukkit.Chunk getChunkAt(int x, int z) {
        return new BukkitChunk(runtime, this, x, z);
    }

    @Override
    public org.bukkit.Chunk getChunkAt(int x, int z, boolean generate) {
        return getChunkAt(x, z);
    }

    @Override
    public org.bukkit.Chunk getChunkAt(Location location) {
        return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public org.bukkit.Chunk getChunkAt(Block block) {
        return getChunkAt(block.getX() >> 4, block.getZ() >> 4);
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        return tube.isChunkLoaded(x, z);
    }

    @Override
    public boolean isChunkLoaded(org.bukkit.Chunk chunk) {
        return tube.isChunkLoaded(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return true;
    }

    @Override
    public boolean loadChunk(int x, int z, boolean generate) {
        tube.loadChunk(x, z).join();
        return true;
    }

    @Override
    public void loadChunk(int x, int z) {
        tube.loadChunk(x, z).join();
    }

    @Override
    public boolean unloadChunk(int x, int z) {
        tube.unloadChunk(x, z);
        return true;
    }

    @Override
    public org.bukkit.Chunk[] getLoadedChunks() {
        return tube.loadedChunks().stream().map(pos -> (org.bukkit.Chunk) new BukkitChunk(runtime, this, pos.x(), pos.z())).toArray(org.bukkit.Chunk[]::new);
    }

    @Override
    public int getChunkCount() {
        return tube.loadedChunks().size();
    }

    @Override
    public java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z) {
        return tube.loadChunk(x, z).thenApply(ignored -> getChunkAt(x, z));
    }

    @Override
    public java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z, boolean generate) {
        return getChunkAtAsync(x, z);
    }

    @Override
    public java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z, boolean generate, boolean urgent) {
        return getChunkAtAsync(x, z);
    }

    @Override
    public java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(Location location) {
        return getChunkAtAsync(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public void save() {
        tube.save();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BukkitWorld world && world.tube().name().equals(tube.name());
    }

    @Override
    public int hashCode() {
        return tube.name().hashCode();
    }

    @Override
    public String toString() {
        return "CraftWorld{name=" + tube.name() + "}";
    }
}
