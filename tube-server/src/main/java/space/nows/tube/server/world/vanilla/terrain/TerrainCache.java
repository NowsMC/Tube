package space.nows.tube.server.world.vanilla.terrain;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class TerrainCache {

    private final TerrainBuilder builder;
    private final int capacity;
    private final Map<Long, CompletableFuture<ChunkDraft>> drafts;

    public TerrainCache(TerrainBuilder builder, int capacity) {
        this.builder = builder;
        this.capacity = capacity;
        this.drafts = new LinkedHashMap<>(capacity, 0.75f, true);
    }

    public ChunkDraft get(int chunkX, int chunkZ) {
        long key = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        CompletableFuture<ChunkDraft> pending;
        boolean mine = false;
        synchronized (drafts) {
            pending = drafts.get(key);
            if (pending == null) {
                pending = new CompletableFuture<>();
                drafts.put(key, pending);
                mine = true;
                evictOldest();
            }
        }
        if (mine) {
            build(key, pending, chunkX, chunkZ);
        }
        return pending.join();
    }

    private void build(long key, CompletableFuture<ChunkDraft> pending, int chunkX, int chunkZ) {
        try {
            pending.complete(builder.build(chunkX, chunkZ));
        } catch (RuntimeException | Error e) {
            pending.completeExceptionally(e);
            synchronized (drafts) {
                drafts.remove(key);
            }
            throw e;
        }
    }

    private void evictOldest() {
        if (drafts.size() > capacity) {
            Iterator<Long> oldest = drafts.keySet().iterator();
            oldest.next();
            oldest.remove();
        }
    }
}
