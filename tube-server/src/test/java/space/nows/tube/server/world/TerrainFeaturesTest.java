package space.nows.tube.server.world;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.ChunkBuilder;
import space.nows.tube.api.world.WorldGenerator;
import space.nows.tube.server.world.generator.NetherGenerator;
import space.nows.tube.server.world.generator.TerrainGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TerrainFeaturesTest {

    @BeforeAll
    static void initMinestom() {
        MinecraftServer.init(new Auth.Offline());
    }

    private static final class Counting implements ChunkBuilder {
        final Map<String, Integer> blocks = new HashMap<>();
        final Set<String> biomes = new HashSet<>();
        int x;
        int z;
        int belowSurfaceAir;

        @Override
        public int chunkX() {
            return x;
        }

        @Override
        public int chunkZ() {
            return z;
        }

        @Override
        public int minY() {
            return -64;
        }

        @Override
        public int maxY() {
            return 320;
        }

        @Override
        public void setBlock(int x, int y, int z, Block block) {
            BlockMapper.toMinestom(block);
            blocks.merge(block.key(), 1, Integer::sum);
        }

        @Override
        public void fillColumn(int x, int z, int fromY, int toY, Block block) {
            BlockMapper.toMinestom(block);
            blocks.merge(block.key(), toY - fromY, Integer::sum);
        }

        @Override
        public void fillLayers(int fromY, int toY, Block block) {
            fillColumn(0, 0, fromY, toY, block);
        }

        @Override
        public void setBiome(int x, int y, int z, String biome) {
            biomes.add(biome);
        }
    }

    private static Counting generate(WorldGenerator generator, int radius) {
        Counting counting = new Counting();
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                counting.x = x * 7;
                counting.z = z * 7;
                generator.generate(counting);
            }
        }
        return counting;
    }

    @Test
    void overworldHasTreesOresCavesAndBiomes() {
        TerrainGenerator generator = new TerrainGenerator(1234);
        generate(generator, 2);
        long start = System.nanoTime();
        Counting counting = generate(generator, 10);
        long perChunk = (System.nanoTime() - start) / 400;
        System.out.println("overworld chunk " + perChunk / 1000 + " us, biomes " + counting.biomes);

        for (String key : new String[]{"oak_log", "oak_leaves", "coal_ore", "iron_ore", "deepslate_diamond_ore", "short_grass", "lava", "water"}) {
            assertTrue(counting.blocks.getOrDefault("minecraft:" + key, 0) > 0, key);
        }
        assertTrue(counting.biomes.size() >= 4, counting.biomes::toString);
    }

    @Test
    void netherHasLavaGlowstoneAndQuartz() {
        Counting counting = generate(new NetherGenerator(99), 3);
        for (String key : new String[]{"netherrack", "lava", "glowstone", "nether_quartz_ore", "bedrock"}) {
            assertTrue(counting.blocks.getOrDefault("minecraft:" + key, 0) > 0, key);
        }
    }
}
