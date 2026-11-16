package org.bukkit;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface World {

    String getName();

    java.io.File getWorldFolder();

    WorldBorder getWorldBorder();

    <T extends org.bukkit.entity.Entity> java.util.Collection<T> getEntitiesByClass(Class<T> type);

    org.bukkit.entity.Item dropItem(Location location, org.bukkit.inventory.ItemStack item);

    org.bukkit.entity.Item dropItemNaturally(Location location, org.bukkit.inventory.ItemStack item);

    UUID getUID();

    NamespacedKey getKey();

    Location getSpawnLocation();

    Environment getEnvironment();

    Block getBlockAt(int x, int y, int z);

    Block getBlockAt(Location location);

    List<Player> getPlayers();

    List<Entity> getEntities();

    int getMinHeight();

    int getMaxHeight();

    int getSeaLevel();

    Chunk getChunkAt(int x, int z);

    Chunk getChunkAt(int x, int z, boolean generate);

    Chunk getChunkAt(Location location);

    Chunk getChunkAt(org.bukkit.block.Block block);

    boolean isChunkLoaded(int x, int z);

    boolean isChunkLoaded(Chunk chunk);

    boolean isChunkGenerated(int x, int z);

    boolean loadChunk(int x, int z, boolean generate);

    void loadChunk(int x, int z);

    boolean unloadChunk(int x, int z);

    Chunk[] getLoadedChunks();

    int getChunkCount();

    java.util.concurrent.CompletableFuture<Chunk> getChunkAtAsync(int x, int z);

    java.util.concurrent.CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean generate);

    java.util.concurrent.CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean generate, boolean urgent);

    java.util.concurrent.CompletableFuture<Chunk> getChunkAtAsync(Location location);

    void save();

    enum Environment {
        NORMAL,
        NETHER,
        THE_END,
        CUSTOM
    }
}
