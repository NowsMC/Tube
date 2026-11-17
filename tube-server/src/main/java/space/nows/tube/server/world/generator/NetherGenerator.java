package space.nows.tube.server.world.generator;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.ChunkBuilder;
import space.nows.tube.api.world.WorldGenerator;

import java.util.Random;

public final class NetherGenerator implements WorldGenerator {

    public static final int LAVA_LEVEL = 31;
    public static final int CEILING = 127;

    private static final Block BEDROCK = Block.of("bedrock");
    private static final Block NETHERRACK = Block.of("netherrack");
    private static final Block LAVA = Block.of("lava");
    private static final Block SOUL_SAND = Block.of("soul_sand");
    private static final Block GRAVEL = Block.of("gravel");
    private static final Block GLOWSTONE = Block.of("glowstone");
    private static final Block QUARTZ = Block.of("nether_quartz_ore");
    private static final Block GOLD = Block.of("nether_gold_ore");

    private final long seed;
    private final CaveNoise density;
    private final FastSimplexNoiseGenerator patches;

    public NetherGenerator(long seed) {
        this.seed = seed;
        this.density = new CaveNoise(seed + 11, 70, 35);
        this.patches = FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 12).build();
    }

    public boolean solid(int x, int y, int z) {
        return solid(density.sample(x, y, z), y);
    }

    private static boolean solid(double noise, int y) {
        double floor = y < 30 ? (30 - y) / 15.0 : 0;
        double roof = y > 100 ? (y - 100) / 12.0 : 0;
        return noise + floor + roof - 0.1 > 0;
    }

    @Override
    public void generate(ChunkBuilder chunk) {
        int baseX = chunk.chunkX() << 4;
        int baseZ = chunk.chunkZ() << 4;
        int maxY = Math.min(chunk.maxY(), CEILING + 1);
        ChunkColumns blocks = new ChunkColumns(chunk.minY(), chunk.maxY());
        double[][][] grid = density.grid(baseX, baseZ, chunk.minY(), maxY);
        Random random = new Random(seed ^ chunk.chunkX() * 341873128712L ^ chunk.chunkZ() * 132897987541L);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.minY(); y < maxY; y++) {
                    if (y == chunk.minY() || y == CEILING || y < chunk.minY() + 4 && random.nextInt(4) >= y - chunk.minY()
                            || y > CEILING - 4 && random.nextInt(4) >= CEILING - y) {
                        blocks.set(x, y, z, BEDROCK);
                    } else if (solid(CaveNoise.at(grid, x, y, z, chunk.minY()), y)) {
                        blocks.set(x, y, z, NETHERRACK);
                    } else if (y <= LAVA_LEVEL) {
                        blocks.set(x, y, z, LAVA);
                    }
                }
                double patch = patches.evaluateNoise((baseX + x) / 24.0, (baseZ + z) / 24.0);
                for (int y = LAVA_LEVEL; y < 45; y++) {
                    if (blocks.get(x, y, z) == NETHERRACK && blocks.isAir(x, y + 1, z) && Math.abs(patch) > 0.45) {
                        blocks.set(x, y, z, patch > 0 ? SOUL_SAND : GRAVEL);
                    }
                }
            }
        }

        for (int vein = 0; vein < 16; vein++) {
            vein(blocks, random, 10 + random.nextInt(108), QUARTZ, 10);
        }
        for (int vein = 0; vein < 6; vein++) {
            vein(blocks, random, 10 + random.nextInt(108), GOLD, 6);
        }
        for (int cluster = 0; cluster < 3; cluster++) {
            glowstone(blocks, random);
        }
        blocks.writeTo(chunk);
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                for (int y = chunk.minY(); y < chunk.maxY(); y += 4) {
                    chunk.setBiome(x, y, z, "minecraft:nether_wastes");
                }
            }
        }
    }

    private static void vein(ChunkColumns blocks, Random random, int y, Block ore, int size) {
        int x = random.nextInt(16);
        int z = random.nextInt(16);
        for (int i = 0; i < size; i++) {
            if (blocks.get(x, y, z) == NETHERRACK) {
                blocks.set(x, y, z, ore);
            }
            x += random.nextInt(3) - 1;
            y += random.nextInt(3) - 1;
            z += random.nextInt(3) - 1;
        }
    }

    private static void glowstone(ChunkColumns blocks, Random random) {
        int x = 2 + random.nextInt(12);
        int z = 2 + random.nextInt(12);
        for (int y = CEILING - 5; y > 70; y--) {
            if (blocks.isAir(x, y, z) && blocks.get(x, y + 1, z) == NETHERRACK) {
                for (int i = 0; i < 25; i++) {
                    int gx = x + random.nextInt(5) - 2;
                    int gy = y - random.nextInt(4);
                    int gz = z + random.nextInt(5) - 2;
                    if (blocks.isAir(gx, gy, gz)) {
                        blocks.set(gx, gy, gz, GLOWSTONE);
                    }
                }
                return;
            }
        }
    }

    @Override
    public int spawnHeight(int x, int z) {
        for (int y = LAVA_LEVEL + 2; y < CEILING - 3; y++) {
            if (solid(x, y - 1, z) && !solid(x, y, z) && !solid(x, y + 1, z)) {
                return y;
            }
        }
        return 64;
    }
}
