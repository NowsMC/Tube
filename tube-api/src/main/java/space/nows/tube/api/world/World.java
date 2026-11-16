package space.nows.tube.api.world;

import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.inventory.ItemStack;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface World {

    String name();

    Path folder();

    WorldOptions options();

    WorldBorder border();

    void setBorder(WorldBorder border);

    void setBorder(WorldBorder border, double transitionSeconds);

    Location spawn();

    Block block(int x, int y, int z);

    void setBlock(int x, int y, int z, Block block);

    Entity spawn(String type, Location location);

    Entity dropItem(Location location, ItemStack item);

    Collection<Entity> entities();

    boolean isChunkLoaded(int chunkX, int chunkZ);

    CompletableFuture<Void> loadChunk(int chunkX, int chunkZ);

    void unloadChunk(int chunkX, int chunkZ);

    List<ChunkPos> loadedChunks();

    void save();
}
