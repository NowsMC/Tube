package space.nows.tube.server.world.vanilla;

import space.nows.tube.api.world.generation.ChunkBuilder;
import space.nows.tube.api.world.generation.WorldGenerator;
import space.nows.tube.server.world.vanilla.biome.BiomeInfo;
import space.nows.tube.server.world.vanilla.biome.BiomeSampler;
import space.nows.tube.server.world.vanilla.biome.BiomeTree;
import space.nows.tube.server.world.vanilla.biome.ChunkCornerBiomes;
import space.nows.tube.server.world.vanilla.biome.OverworldBiomeLayout;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.carver.CaveCarving;
import space.nows.tube.server.world.vanilla.density.DensityReader;
import space.nows.tube.server.world.vanilla.feature.ChunkDecorator;
import space.nows.tube.server.world.vanilla.feature.FeatureRegistry;
import space.nows.tube.server.world.vanilla.noise.WorldNoises;
import space.nows.tube.server.world.vanilla.surface.SurfacePainter;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;
import space.nows.tube.server.world.vanilla.terrain.TerrainBuilder;
import space.nows.tube.server.world.vanilla.terrain.TerrainCache;
import space.nows.tube.server.world.vanilla.terrain.TerrainRouter;
import space.nows.tube.server.world.vanilla.terrain.TerrainSettings;

import java.util.Map;
import java.util.Set;

public final class VanillaGenerator implements WorldGenerator {

    private static final int CACHED_CHUNKS = 160;
    private static final int OPEN_DRAFTS = 512;
    private static final int DECORATED_SOURCES = 65536;
    private static final double LAND_FROM = -0.11;
    private static final double RIVER_WITHIN = 0.16;

    private final TerrainSettings settings;
    private final TerrainCache terrain;
    private final FeatureRegistry features;
    private final DecoratedChunks decorated;

    public VanillaGenerator(WorldgenData data, long seed) {
        WorldNoises noises = new WorldNoises(data, seed);
        this.settings = TerrainSettings.read(data.get("noise_settings", "overworld"), new DensityReader(data, noises));
        Map<String, BiomeInfo> biomes = BiomeInfo.readAll(data);
        BiomeSampler sampler = new BiomeSampler(new BiomeTree(OverworldBiomeLayout.spots()), settings.router());
        SurfacePainter painter = new SurfacePainter(settings, noises, biomes);
        ChunkCornerBiomes cornerBiomes = new ChunkCornerBiomes(sampler, biomes);
        CaveCarving carving = new CaveCarving(data, seed, settings, cornerBiomes::at);
        TerrainBuilder builder = new TerrainBuilder(settings, noises.named("minecraft:aquifer").split(), noises.named("minecraft:ore").split(),
                (draft, density, water) -> sampler.fill(draft, density),
                (draft, density, water) -> painter.paint(draft, density),
                (draft, density, water) -> carving.carve(draft, water));
        this.terrain = new TerrainCache(builder, CACHED_CHUNKS);
        this.features = new FeatureRegistry(data, settings.minY(), settings.height());
        ChunkDecorator decorator = new ChunkDecorator(seed, biomes, features, settings.seaLevel());
        this.decorated = new DecoratedChunks(terrain, decorator, OPEN_DRAFTS, DECORATED_SOURCES);
    }

    @Override
    public void generate(ChunkBuilder chunk) {
        DraftWriter.write(decorated.finish(chunk.chunkX(), chunk.chunkZ()), chunk);
    }

    @Override
    public boolean canSpawnAt(int x, int z) {
        TerrainRouter router = settings.router();
        if (router.continentalness().at(x, 0, z) < LAND_FROM || Math.abs(router.weirdness().at(x, 0, z)) < RIVER_WITHIN) {
            return false;
        }
        ChunkDraft draft = terrain.get(x >> 4, z >> 4);
        int top = draft.height(Heightmap.SOLID_OR_FLUID, x & 15, z & 15);
        return top > settings.seaLevel() && !draft.get(x & 15, top - 1, z & 15).hasFluid();
    }

    @Override
    public int spawnHeight(int x, int z) {
        return terrain.get(x >> 4, z >> 4).height(Heightmap.SOLID_OR_FLUID, x & 15, z & 15);
    }

    public Set<String> skippedFeatures() {
        return features.skipped();
    }
}
