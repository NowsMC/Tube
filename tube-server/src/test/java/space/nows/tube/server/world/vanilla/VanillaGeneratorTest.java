package space.nows.tube.server.world.vanilla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.World;
import space.nows.tube.api.world.WorldOptions;
import space.nows.tube.api.world.generation.ChunkBuilder;
import space.nows.tube.server.vanilla.MojangServerJar;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;
import space.nows.tube.server.world.generator.SpawnSearch;
import space.nows.tube.server.world.vanilla.feature.FeatureRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

class VanillaGeneratorTest {

    private static WorldgenData data;

    @BeforeAll
    static void load(@TempDir Path directory) throws Exception {
        MinecraftServer.init(new Auth.Offline());
        String extracted = System.getProperty("tube.worldgenZip");
        if (extracted != null && Files.isRegularFile(Path.of(extracted))) {
            data = WorldgenData.read(Path.of(extracted));
            return;
        }
        String jar = System.getProperty("tube.vanillaJar");
        if (jar == null || !Files.isRegularFile(Path.of(jar))) {
            return;
        }
        Path zip = directory.resolve("worldgen.zip");
        MojangServerJar.extract(Path.of(jar), "26.2", "data/minecraft/worldgen/", zip);
        data = WorldgenData.read(zip);
    }

    static final class Column implements ChunkBuilder {
        final Block[] blocks = new Block[16 * 16 * 384];
        final Set<String> biomes = new TreeSet<>();
        final int x;
        final int z;

        Column(int x, int z) {
            this.x = x;
            this.z = z;
        }

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
            blocks[(y + 64) << 8 | z << 4 | x] = block;
        }

        @Override
        public void fillColumn(int x, int z, int fromY, int toY, Block block) {
            for (int y = fromY; y < toY; y++) {
                setBlock(x, y, z, block);
            }
        }

        @Override
        public void fillLayers(int fromY, int toY, Block block) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    fillColumn(x, z, fromY, toY, block);
                }
            }
        }

        @Override
        public void setBiome(int x, int y, int z, String biome) {
            biomes.add(biome);
        }

        Block get(int x, int y, int z) {
            return blocks[(y + 64) << 8 | z << 4 | x];
        }

        int top(int x, int z) {
            for (int y = 319; y >= -64; y--) {
                Block block = get(x, y, z);
                if (block != null) {
                    return y;
                }
            }
            return -64;
        }
    }

    @Test
    void generatesVanillaLikeTerrain() {
        assumeTrue(data != null);
        VanillaGenerator generator = new VanillaGenerator(data, 2890775764613289345L);
        Map<String, Integer> surface = new HashMap<>();
        Map<String, Integer> all = new HashMap<>();
        Set<String> biomes = new TreeSet<>();
        int radius = 4;
        long start = System.nanoTime();
        for (int cx = -radius; cx < radius; cx++) {
            for (int cz = -radius; cz < radius; cz++) {
                Column column = new Column(cx, cz);
                generator.generate(column);
                biomes.addAll(column.biomes);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        surface.merge(column.get(x, column.top(x, z), z).key(), 1, Integer::sum);
                        for (int y = -64; y < 320; y++) {
                            Block block = column.get(x, y, z);
                            if (block != null) {
                                all.merge(block.key(), 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }
        long chunks = 4L * radius * radius;
        System.out.println("vanilla chunk " + (System.nanoTime() - start) / chunks / 1_000_000 + " ms, biomes " + biomes);
        System.out.println("surface " + surface);
        System.out.println("skipped " + generator.skippedFeatures());

        for (String key : new String[]{"stone", "deepslate", "bedrock", "coal_ore", "iron_ore", "water"}) {
            assertTrue(all.getOrDefault("minecraft:" + key, 0) > 0, key);
        }
        assertTrue(biomes.size() >= 2, biomes::toString);
    }

    @Test
    void generatesMinestomChunksAroundTheSpawn(@TempDir Path directory) {
        assumeTrue(data != null);
        WorldService service = new WorldService(directory, "main", WorldOptions.of(WorldOptions.NORMAL, 99), seed -> new VanillaGenerator(data, seed));
        World world = service.create("vanilla", WorldOptions.of(WorldOptions.NORMAL, 99));
        int spawnX = (int) Math.floor(world.spawn().x());
        int spawnZ = (int) Math.floor(world.spawn().z());
        int spawnY = (int) world.spawn().y();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.loadChunk((spawnX >> 4) + dx, (spawnZ >> 4) + dz).join();
            }
        }
        Block ground = world.block(spawnX, spawnY - 1, spawnZ);
        assertFalse(ground.isAir(), ground::toString);
        assertNotEquals("minecraft:water", ground.key());
        assertEquals("minecraft:bedrock", world.block(spawnX, -64, spawnZ).key());
        MinestomWorld minestom = (MinestomWorld) world;
        List.copyOf(minestom.instance().getChunks()).forEach(minestom.instance()::unloadChunk);
    }

    @Test
    void readsEveryPlacedFeature() {
        assumeTrue(data != null);
        FeatureRegistry features = new FeatureRegistry(data, -64, 384);
        for (String id : data.names("placed_feature")) {
            features.placed(id);
        }
        for (String reason : features.skipped()) {
            assertFalse(reason.contains("Exception"), reason);
        }
    }

    @Test
    void findsDrySpawn() {
        assumeTrue(data != null);
        VanillaGenerator generator = new VanillaGenerator(data, 42);
        int[] spawn = SpawnSearch.find(generator);
        assertTrue(generator.canSpawnAt(spawn[0], spawn[1]));
        assertTrue(generator.spawnHeight(spawn[0], spawn[1]) > 63);
    }
}
