package space.nows.tube.server.world.vanilla;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.server.world.vanilla.feature.ChunkDecorator;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;
import space.nows.tube.server.world.vanilla.terrain.TerrainCache;

import java.util.LinkedHashMap;
import java.util.Map;

final class DecoratedChunks {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final int REACH = DecorationArea.CHUNKS_ACROSS / 2;

    private final TerrainCache terrain;
    private final ChunkDecorator decorator;
    private final Map<Long, ChunkDraft> drafts;
    private final Map<Long, Boolean> decorated;

    DecoratedChunks(TerrainCache terrain, ChunkDecorator decorator, int draftCapacity, int sourceCapacity) {
        this.terrain = terrain;
        this.decorator = decorator;
        this.drafts = lru(draftCapacity);
        this.decorated = lru(sourceCapacity);
    }

    ChunkDraft finish(int chunkX, int chunkZ) {
        for (int dz = -2 * REACH; dz <= 2 * REACH; dz++) {
            for (int dx = -2 * REACH; dx <= 2 * REACH; dx++) {
                terrain.get(chunkX + dx, chunkZ + dz);
            }
        }
        synchronized (this) {
            for (int dz = -REACH; dz <= REACH; dz++) {
                for (int dx = -REACH; dx <= REACH; dx++) {
                    decorate(chunkX + dx, chunkZ + dz);
                }
            }
            ChunkDraft done = draft(chunkX, chunkZ);
            drafts.remove(key(chunkX, chunkZ));
            return done;
        }
    }

    private void decorate(int chunkX, int chunkZ) {
        if (decorated.put(key(chunkX, chunkZ), Boolean.TRUE) != null) {
            return;
        }
        int across = DecorationArea.CHUNKS_ACROSS;
        ChunkDraft[] area = new ChunkDraft[across * across];
        for (int row = 0; row < across; row++) {
            for (int column = 0; column < across; column++) {
                area[row * across + column] = draft(chunkX + column - REACH, chunkZ + row - REACH);
            }
        }
        try {
            decorator.decorate(area);
        } catch (RuntimeException e) {
            LOGGER.warn("World generation could not decorate chunk {}, {}", chunkX, chunkZ, e);
        }
    }

    private ChunkDraft draft(int chunkX, int chunkZ) {
        return drafts.computeIfAbsent(key(chunkX, chunkZ), key -> terrain.get(chunkX, chunkZ).copy());
    }

    private static long key(int chunkX, int chunkZ) {
        return (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
    }

    private static <V> Map<Long, V> lru(int capacity) {
        return new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, V> eldest) {
                return size() > capacity;
            }
        };
    }
}
