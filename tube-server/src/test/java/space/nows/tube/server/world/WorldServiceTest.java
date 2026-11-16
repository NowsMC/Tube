package space.nows.tube.server.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class WorldServiceTest {

    @TempDir
    Path directory;

    @BeforeAll
    static void initMinestom() {
        MinecraftServer.init(new Auth.Offline());
    }

    @Test
    void persistsBlocksAcrossLoads() {
        WorldService first = new WorldService(directory, "main");
        MinestomWorld world = (MinestomWorld) first.defaultWorld();
        world.instance().loadChunk(0, 0).join();
        world.instance().setBlock(new Vec(1, 60, 1), Block.DIAMOND_BLOCK);
        first.saveAll();
        assertTrue(Files.exists(directory.resolve("main/dimensions/minecraft/overworld/region/r.0.0.mca")));
        release(world);

        WorldService second = new WorldService(directory, "main");
        MinestomWorld reloaded = (MinestomWorld) second.defaultWorld();
        reloaded.instance().loadChunk(0, 0).join();
        assertEquals(Block.DIAMOND_BLOCK, reloaded.instance().getBlock(new Vec(1, 60, 1)));
        release(reloaded);
    }

    private static void release(MinestomWorld world) {
        List.copyOf(world.instance().getChunks()).forEach(world.instance()::unloadChunk);
    }

    @Test
    void blocksCanBeReadAndWrittenThroughTheApi() {
        WorldService service = new WorldService(directory, "main");
        MinestomWorld world = (MinestomWorld) service.defaultWorld();

        world.setBlock(2, 60, 2, space.nows.tube.api.Block.of("stone"));
        assertEquals("minecraft:stone", world.block(2, 60, 2).key());

        world.setBlock(3, 60, 3, new space.nows.tube.api.Block("minecraft:oak_stairs", java.util.Map.of("facing", "north")));
        assertEquals("north", world.block(3, 60, 3).properties().get("facing"));

        assertTrue(world.block(100, 100, 100).isAir());
        assertThrows(IllegalArgumentException.class, () -> world.setBlock(0, 60, 0, space.nows.tube.api.Block.of("tube:nonexistent")));
        release(world);
    }

    @Test
    void rejectsUnsafeWorldNames() {
        WorldService service = new WorldService(directory, "main");
        assertThrows(IllegalArgumentException.class, () -> service.load("../escape"));
    }
}
