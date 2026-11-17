package space.nows.tube.server.gameplay.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class PhysicsTest {

    private Instance setup(Env env) {
        new Fluids().install(MinecraftServer.getGlobalEventHandler());
        new FallingBlocks().install(MinecraftServer.getGlobalEventHandler());
        Instance instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        return instance;
    }

    private static void ticks(Env env, int count) {
        for (int i = 0; i < count; i++) {
            env.tick();
        }
    }

    @Test
    void waterSpreadsSevenBlocksOnFlatGround(Env env) {
        Instance instance = setup(env);
        instance.setBlock(8, 40, 8, Block.WATER);
        ticks(env, 120);
        assertEquals("0", instance.getBlock(8, 40, 8).getProperty("level"));
        assertTrue(instance.getBlock(15, 40, 8).compare(Block.WATER), () -> instance.getBlock(15, 40, 8).toString());
        assertEquals("7", instance.getBlock(15, 40, 8).getProperty("level"));
        assertTrue(instance.getBlock(16, 40, 8).air());
        assertTrue(instance.getBlock(8, 41, 8).air());

        instance.setBlock(8, 40, 8, Block.AIR);
        ticks(env, 120);
        assertTrue(instance.getBlock(12, 40, 8).air(), () -> instance.getBlock(12, 40, 8).toString());
    }

    @Test
    void waterFallsDownAndLavaMeetingWaterTurnsToStone(Env env) {
        Instance instance = setup(env);
        instance.setBlock(0, 45, 0, Block.WATER);
        ticks(env, 60);
        assertTrue(instance.getBlock(0, 41, 0).compare(Block.WATER));
        assertTrue(instance.getBlock(0, 40, 0).compare(Block.WATER));

        instance.setBlock(20, 40, 20, Block.WATER);
        instance.setBlock(21, 40, 20, Block.LAVA);
        ticks(env, 40);
        assertTrue(instance.getBlock(21, 40, 20).compare(Block.OBSIDIAN), () -> instance.getBlock(21, 40, 20).toString());
    }

    @Test
    void sandFallsUntilItLands(Env env) {
        Instance instance = setup(env);
        instance.setBlock(3, 41, 3, Block.STONE);
        instance.setBlock(3, 47, 3, Block.SAND);
        instance.setBlock(3, 48, 3, Block.SAND);
        ticks(env, 80);
        assertTrue(instance.getBlock(3, 42, 3).compare(Block.SAND), () -> instance.getBlock(3, 42, 3).toString());
        assertTrue(instance.getBlock(3, 43, 3).compare(Block.SAND));
        assertTrue(instance.getBlock(3, 47, 3).air());
    }

    @Test
    void obsidianFramesLightIntoPortals(Env env) {
        Instance instance = setup(env);
        for (int x = 0; x < 4; x++) {
            instance.setBlock(x, 40, 5, Block.OBSIDIAN);
            instance.setBlock(x, 44, 5, Block.OBSIDIAN);
        }
        for (int y = 41; y < 44; y++) {
            instance.setBlock(0, y, 5, Block.OBSIDIAN);
            instance.setBlock(3, y, 5, Block.OBSIDIAN);
        }
        assertTrue(new NetherPortals(null).tryLight(instance, new BlockVec(1, 41, 5)));
        assertTrue(instance.getBlock(2, 43, 5).compare(Block.NETHER_PORTAL));
        assertEquals("x", instance.getBlock(1, 41, 5).getProperty("axis"));
    }
}
