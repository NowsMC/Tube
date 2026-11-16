package space.nows.tube.server.world.generator;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.ChunkBuilder;
import space.nows.tube.api.world.WorldGenerator;

public final class TerrainGenerator implements WorldGenerator {

    public static final int SEA_LEVEL = 62;

    private static final Block BEDROCK = Block.of("bedrock");
    private static final Block DEEPSLATE = Block.of("deepslate");
    private static final Block STONE = Block.of("stone");
    private static final Block DIRT = Block.of("dirt");
    private static final Block GRASS = Block.of("grass_block");
    private static final Block SAND = Block.of("sand");
    private static final Block GRAVEL = Block.of("gravel");
    private static final Block WATER = Block.of("water");

    private final FastSimplexNoiseGenerator continents;
    private final FastSimplexNoiseGenerator hills;
    private final FastSimplexNoiseGenerator detail;

    public TerrainGenerator(long seed) {
        this.continents = FastSimplexNoiseGenerator.newBuilder().setSeed(seed).build();
        this.hills = FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 1).build();
        this.detail = FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 2).build();
    }

    public int height(int x, int z) {
        double land = continents.evaluateNoise(x / 700.0, z / 700.0);
        double roughness = Math.max(0, land + 0.2) * 22;
        double surface = 64 + land * 20 + hills.evaluateNoise(x / 160.0, z / 160.0) * roughness + detail.evaluateNoise(x / 40.0, z / 40.0) * 3;
        return (int) Math.round(surface);
    }

    @Override
    public void generate(ChunkBuilder chunk) {
        int minY = chunk.minY();
        int maxY = chunk.maxY();
        int baseX = chunk.chunkX() << 4;
        int baseZ = chunk.chunkZ() << 4;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = Math.clamp(height(baseX + x, baseZ + z), minY + 5, maxY - 2);
                boolean shore = top <= SEA_LEVEL + 1;
                chunk.fillColumn(x, z, minY, minY + 1, BEDROCK);
                chunk.fillColumn(x, z, minY + 1, Math.min(0, top - 3), DEEPSLATE);
                chunk.fillColumn(x, z, Math.max(0, minY + 1), top - 3, STONE);
                chunk.fillColumn(x, z, top - 3, top, shore ? SAND : DIRT);
                chunk.setBlock(x, top, z, top >= SEA_LEVEL ? (shore ? SAND : GRASS) : top >= SEA_LEVEL - 4 ? SAND : GRAVEL);
                if (top < SEA_LEVEL) {
                    chunk.fillColumn(x, z, top + 1, SEA_LEVEL + 1, WATER);
                }
            }
        }
    }

    @Override
    public int spawnHeight(int x, int z) {
        return Math.max(height(x, z), SEA_LEVEL) + 1;
    }
}
