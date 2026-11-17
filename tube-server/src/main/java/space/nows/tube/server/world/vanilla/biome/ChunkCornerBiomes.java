package space.nows.tube.server.world.vanilla.biome;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ChunkCornerBiomes {

    private static final int CAPACITY = 4096;

    private final BiomeSampler sampler;
    private final Map<String, BiomeInfo> biomes;
    private final Map<Long, BiomeInfo> known = new LinkedHashMap<>(CAPACITY, 0.75f, true);

    public ChunkCornerBiomes(BiomeSampler sampler, Map<String, BiomeInfo> biomes) {
        this.sampler = sampler;
        this.biomes = biomes;
    }

    public BiomeInfo at(int chunkX, int chunkZ) {
        long key = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        synchronized (known) {
            BiomeInfo cached = known.get(key);
            if (cached != null) {
                return cached;
            }
        }
        BiomeInfo biome = biomes.get(sampler.at(chunkX << 4, 0, chunkZ << 4));
        synchronized (known) {
            known.put(key, biome);
            if (known.size() > CAPACITY) {
                Iterator<Long> oldest = known.keySet().iterator();
                oldest.next();
                oldest.remove();
            }
        }
        return biome;
    }
}
