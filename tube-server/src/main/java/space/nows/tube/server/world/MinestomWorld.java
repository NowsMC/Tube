package space.nows.tube.server.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.InstanceContainer;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.inventory.ItemStack;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;
import space.nows.tube.server.inventory.ItemMapper;

import java.nio.file.Path;
import java.util.Collection;

public final class MinestomWorld implements World {

    private final String name;
    private final InstanceContainer instance;
    private final double[] spawn;
    private final WorldService owner;

    MinestomWorld(String name, InstanceContainer instance, double[] spawn, WorldService owner) {
        this.name = name;
        this.instance = instance;
        this.spawn = spawn;
        this.owner = owner;
    }

    public InstanceContainer instance() {
        return instance;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Path folder() {
        return owner.folderOf(name);
    }

    @Override
    public Location spawn() {
        return new Location(this, spawn[0], spawn[1], spawn[2]);
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return instance.getChunk(chunkX, chunkZ) != null;
    }

    @Override
    public java.util.concurrent.CompletableFuture<Void> loadChunk(int chunkX, int chunkZ) {
        return instance.loadChunk(chunkX, chunkZ).thenApply(chunk -> null);
    }

    @Override
    public void unloadChunk(int chunkX, int chunkZ) {
        var chunk = instance.getChunk(chunkX, chunkZ);
        if (chunk != null) {
            instance.unloadChunk(chunk);
        }
    }

    @Override
    public java.util.List<space.nows.tube.api.world.ChunkPos> loadedChunks() {
        return instance.getChunks().stream().map(chunk -> new space.nows.tube.api.world.ChunkPos(chunk.getChunkX(), chunk.getChunkZ())).toList();
    }

    @Override
    public Block block(int x, int y, int z) {
        instance.loadChunk(x >> 4, z >> 4).join();
        return BlockMapper.fromMinestom(instance.getBlock(x, y, z));
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        net.minestom.server.instance.block.Block resolved = BlockMapper.toMinestom(block);
        instance.loadChunk(x >> 4, z >> 4).join();
        instance.setBlock(x, y, z, resolved);
    }

    @Override
    public Entity spawn(String type, Location location) {
        requireHere(location);
        EntityType entityType = EntityType.fromKey(type.indexOf(':') < 0 ? "minecraft:" + type : type);
        if (entityType == null) {
            throw new IllegalArgumentException("Unknown entity type: " + type);
        }
        if (entityType == EntityType.PLAYER) {
            throw new IllegalArgumentException("Players cannot be spawned");
        }
        net.minestom.server.entity.Entity entity = entityType.registry().shouldSendAttributes()
                ? new EntityCreature(entityType)
                : new net.minestom.server.entity.Entity(entityType);
        entity.setInstance(instance, position(location)).join();
        return owner.wrap(entity);
    }

    @Override
    public Entity dropItem(Location location, ItemStack item) {
        requireHere(location);
        ItemEntity entity = new ItemEntity(ItemMapper.toMinestom(item));
        entity.setInstance(instance, position(location)).join();
        return owner.wrap(entity);
    }

    @Override
    public Collection<Entity> entities() {
        return instance.getEntities().stream().map(owner::wrap).toList();
    }

    @Override
    public void save() {
        instance.saveInstance().join();
        instance.saveChunksToStorage().join();
    }

    private void requireHere(Location location) {
        if (!equals(location.world())) {
            throw new IllegalArgumentException("Location belongs to world " + location.world().name() + ", not " + name);
        }
    }

    private static Pos position(Location location) {
        return new Pos(location.x(), location.y(), location.z(), location.yaw(), location.pitch());
    }
}
