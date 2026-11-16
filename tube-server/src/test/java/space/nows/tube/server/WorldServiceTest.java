package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Auth;
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
        TubeWorld world = (TubeWorld) first.defaultWorld();
        world.instance().loadChunk(0, 0).join();
        world.instance().setBlock(new Vec(1, 60, 1), Block.DIAMOND_BLOCK);
        first.saveAll();
        assertTrue(Files.exists(directory.resolve("main/dimensions/minecraft/overworld/region/r.0.0.mca")));
        release(world);

        WorldService second = new WorldService(directory, "main");
        TubeWorld reloaded = (TubeWorld) second.defaultWorld();
        reloaded.instance().loadChunk(0, 0).join();
        assertEquals(Block.DIAMOND_BLOCK, reloaded.instance().getBlock(new Vec(1, 60, 1)));
        release(reloaded);
    }

    private static void release(TubeWorld world) {
        List.copyOf(world.instance().getChunks()).forEach(world.instance()::unloadChunk);
    }

    @Test
    void rejectsUnsafeWorldNames() {
        WorldService service = new WorldService(directory, "main");
        assertThrows(IllegalArgumentException.class, () -> service.load("../escape"));
    }
}
