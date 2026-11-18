package space.nows.tube.server.gameplay.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class PlantSupportTest {

    private Instance meadow(Env env) {
        BlockInteractions.install(MinecraftServer.getGlobalEventHandler());
        Instance instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        for (int x = 4; x <= 8; x++) {
            for (int z = 4; z <= 8; z++) {
                instance.setBlock(x, 39, z, Block.GRASS_BLOCK, false);
                instance.setBlock(x, 40, z, Block.SHORT_GRASS, false);
            }
        }
        return instance;
    }

    @Test
    void breakingAPlantKeepsItsNeighbours(Env env) {
        Instance instance = meadow(env);

        instance.setBlock(6, 40, 6, Block.AIR);

        for (int x = 4; x <= 8; x++) {
            for (int z = 4; z <= 8; z++) {
                if (x != 6 || z != 6) {
                    assertEquals(Block.SHORT_GRASS, instance.getBlock(x, 40, z), "plant at " + x + " " + z);
                }
            }
        }
    }

    @Test
    void placingABlockBesideAPlantKeepsIt(Env env) {
        Instance instance = meadow(env);

        instance.setBlock(6, 41, 6, Block.STONE);
        instance.setBlock(6, 40, 6, Block.COBBLESTONE);

        assertEquals(Block.SHORT_GRASS, instance.getBlock(5, 40, 6));
        assertEquals(Block.SHORT_GRASS, instance.getBlock(7, 40, 6));
        assertEquals(Block.SHORT_GRASS, instance.getBlock(6, 40, 5));
        assertTrue(instance.getBlock(6, 40, 7).compare(Block.SHORT_GRASS));
    }

    @Test
    void breakingTheSoilRemovesOnlyThePlantOnIt(Env env) {
        Instance instance = meadow(env);

        instance.setBlock(6, 39, 6, Block.AIR);

        assertEquals(Block.AIR, instance.getBlock(6, 40, 6));
        assertEquals(Block.SHORT_GRASS, instance.getBlock(5, 40, 6));
        assertEquals(Block.SHORT_GRASS, instance.getBlock(7, 40, 6));
    }
}
