package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class FishTest {

    private Instance instance;

    private void setup(Env env) {
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        env.createPlayer(instance, new Pos(12, 40, 12));
    }

    private void pool() {
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                for (int y = 40; y <= 46; y++) {
                    instance.setBlock(x, y, z, Block.WATER, false);
                }
            }
        }
    }

    private Creature spawn(EntityType type, Pos at) {
        Creature creature = new Creature(CreatureKinds.of(type).orElseThrow());
        CreatureKinds.spawn(creature, instance, at).join();
        return creature;
    }

    private static void ticks(Env env, int count) {
        for (int i = 0; i < count; i++) {
            env.tick();
        }
    }

    @Test
    void aCodSwimsAroundInsideTheWater(Env env) {
        setup(env);
        pool();
        Creature cod = spawn(EntityType.COD, new Pos(0.5, 43, 0.5));
        Pos start = cod.getPosition();

        ticks(env, 400);

        assertTrue(cod.getPosition().distance(start) > 1, "the cod stayed at " + cod.getPosition());
        assertTrue(cod.inWater(), "the cod left the water at " + cod.getPosition());
        assertTrue(!cod.isDead());
    }

    @Test
    void aFishStaysAfloatInsteadOfSinking(Env env) {
        setup(env);
        pool();
        Creature salmon = spawn(EntityType.SALMON, new Pos(0.5, 45, 0.5));

        ticks(env, 60);

        assertTrue(salmon.getPosition().y() > 41, "the salmon sank to " + salmon.getPosition());
    }

    @Test
    void aFishOutOfWaterDiesAfterAWhile(Env env) {
        setup(env);
        Creature cod = spawn(EntityType.COD, new Pos(0.5, 40, 0.5));

        ticks(env, 200);

        assertTrue(cod.isDead() || cod.isRemoved(), "the cod is still alive with " + cod.getHealth());
    }
}
