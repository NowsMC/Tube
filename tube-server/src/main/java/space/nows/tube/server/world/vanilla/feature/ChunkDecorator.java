package space.nows.tube.server.world.vanilla.feature;

import space.nows.tube.server.world.vanilla.biome.BiomeInfo;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ChunkDecorator {

    private static final long STEP_SPACING = 10000L;

    private final long seed;
    private final Map<String, BiomeInfo> biomes;
    private final FeatureRegistry features;
    private final List<List<String>> order;
    private final int seaLevel;

    public ChunkDecorator(long seed, Map<String, BiomeInfo> biomes, FeatureRegistry features, int seaLevel) {
        this.seed = seed;
        this.biomes = biomes;
        this.features = features;
        this.order = FeatureOrder.of(biomes);
        this.seaLevel = seaLevel;
    }

    public void decorate(ChunkDraft[] area) {
        ChunkDraft source = area[area.length / 2];
        DecorationArea region = new DecorationArea(area, source.chunkX, source.chunkZ, seaLevel, biomes);
        DecorationPass pass = new DecorationPass(region, source.chunkX, source.chunkZ);
        Set<BiomeInfo> present = biomesIn(source);
        for (int step = 0; step < order.size(); step++) {
            decorateStep(pass, source, present, step);
        }
    }

    private void decorateStep(DecorationPass pass, ChunkDraft source, Set<BiomeInfo> present, int step) {
        Xoroshiro random = new Xoroshiro(seed);
        long xFactor = random.nextLong() | 1L;
        long zFactor = random.nextLong() | 1L;
        long chunkSeed = (long) source.minX() * xFactor + (long) source.minZ() * zFactor ^ seed;
        List<String> featuresInStep = order.get(step);
        for (int index = 0; index < featuresInStep.size(); index++) {
            String id = featuresInStep.get(index);
            if (!anyHas(present, step, id)) {
                continue;
            }
            random.reseed(chunkSeed + index + STEP_SPACING * step);
            pass.start(id);
            features.placed(id).decorate(pass, random, source.minX(), source.minY, source.minZ());
        }
    }

    private Set<BiomeInfo> biomesIn(ChunkDraft source) {
        Set<String> keys = new HashSet<>();
        for (int quartY = 0; quartY < source.quartHeight(); quartY++) {
            for (int quartZ = 0; quartZ < 4; quartZ++) {
                for (int quartX = 0; quartX < 4; quartX++) {
                    keys.add(source.quartBiome(quartX, quartY, quartZ));
                }
            }
        }
        Set<BiomeInfo> present = Collections.newSetFromMap(new IdentityHashMap<>());
        for (String key : keys) {
            BiomeInfo biome = biomes.get(key);
            if (biome != null) {
                present.add(biome);
            }
        }
        return present;
    }

    private static boolean anyHas(Set<BiomeInfo> present, int step, String feature) {
        for (BiomeInfo biome : present) {
            if (biome.hasFeature(step, feature)) {
                return true;
            }
        }
        return false;
    }
}
