package space.nows.tube.server.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.Environment;
import space.nows.tube.api.world.World;
import space.nows.tube.api.world.WorldOptions;
import space.nows.tube.server.world.generator.TerrainGenerator;

import java.nio.file.Path;
import java.util.List;

class WorldCreationTest {

    @TempDir
    Path directory;

    @BeforeAll
    static void initMinestom() {
        MinecraftServer.init(new Auth.Offline());
    }

    private static void release(World world) {
        MinestomWorld minestom = (MinestomWorld) world;
        List.copyOf(minestom.instance().getChunks()).forEach(minestom.instance()::unloadChunk);
    }

    @Test
    void terrainDependsOnlyOnTheSeed() {
        TerrainGenerator a = new TerrainGenerator(42);
        TerrainGenerator b = new TerrainGenerator(42);
        TerrainGenerator c = new TerrainGenerator(43);
        boolean differs = false;
        for (int x = -2000; x <= 2000; x += 250) {
            assertEquals(a.height(x, x / 2), b.height(x, x / 2));
            differs |= a.height(x, x / 2) != c.height(x, x / 2);
        }
        assertTrue(differs);
    }

    @Test
    void normalWorldsSpawnOnTheirSurface() {
        WorldService service = new WorldService(directory, "main");
        World world = service.create("terrain", WorldOptions.of(WorldOptions.NORMAL, 7));
        int spawnX = (int) Math.floor(world.spawn().x());
        int spawnZ = (int) Math.floor(world.spawn().z());
        world.loadChunk(spawnX >> 4, spawnZ >> 4).join();

        int spawnY = (int) world.spawn().y();
        assertTrue(world.block(spawnX, spawnY, spawnZ).isAir());
        assertFalse(world.block(spawnX, spawnY - 1, spawnZ).isAir());
        assertFalse(world.block(spawnX, spawnY - 1, spawnZ).key().equals("minecraft:water"));
        assertEquals("minecraft:bedrock", world.block((spawnX & ~15) + 3, -64, (spawnZ & ~15) + 3).key());
        release(world);
    }

    @Test
    void voidWorldsOnlyHaveASpawnBlock() {
        WorldService service = new WorldService(directory, "main");
        World world = service.create("empty", WorldOptions.of(WorldOptions.VOID, 0));
        world.loadChunk(0, 0).join();

        assertEquals("minecraft:stone", world.block(0, 63, 0).key());
        assertTrue(world.block(5, 63, 5).isAir());
        assertTrue(world.block(5, 0, 5).isAir());
        release(world);
    }

    @Test
    void storedOptionsWinOverLaterRequests() {
        WorldService first = new WorldService(directory, "main");
        World created = first.create("kept", new WorldOptions(WorldOptions.VOID, 99, Environment.NETHER));
        release(created);

        WorldService second = new WorldService(directory, "main");
        World reopened = second.create("kept", WorldOptions.of(WorldOptions.NORMAL, 1));
        assertEquals(new WorldOptions(WorldOptions.VOID, 99, Environment.NETHER), reopened.options());
        release(reopened);
    }

    @Test
    void pluginsCanRegisterGenerators() {
        WorldService service = new WorldService(directory, "main");
        service.registerGenerator("demo:glass", seed -> chunk -> chunk.fillLayers(10, 11, Block.of("glass")));
        assertThrows(IllegalArgumentException.class, () -> service.registerGenerator("demo:glass", seed -> chunk -> { }));
        assertTrue(service.generators().contains("demo:glass"));

        World world = service.create("glass", WorldOptions.of("demo:glass", 0));
        world.loadChunk(1, 1).join();
        assertEquals("minecraft:glass", world.block(20, 10, 20).key());
        release(world);
    }

    @Test
    void onlyEmptyNonDefaultWorldsUnload() {
        WorldService service = new WorldService(directory, "main");
        service.defaultWorld();
        World extra = service.create("extra", WorldOptions.of(WorldOptions.FLAT, 0));

        assertFalse(service.unload("main", false));
        assertTrue(service.unload("extra", false));
        assertTrue(service.get("extra").isEmpty());
        assertNotEquals(extra, service.defaultWorld());
    }
}
