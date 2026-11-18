package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class CrowdingTest {

    private Instance instance;

    private void setup(Env env) {
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        env.createPlayer(instance, new Pos(10, 40, 10));
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
    void overlappingMobsPushEachOtherApart(Env env) {
        setup(env);
        Creature first = spawn(EntityType.COW, new Pos(0, 40, 0));
        Creature second = spawn(EntityType.COW, new Pos(0.2, 40, 0));

        ticks(env, 40);

        assertTrue(first.getPosition().distance(second.getPosition()) > 0.6, "cows are " + first.getPosition().distance(second.getPosition()) + " apart");
    }

    @Test
    void mobsOnTheExactSameSpotStillSeparate(Env env) {
        setup(env);
        Creature first = spawn(EntityType.PIG, new Pos(0, 40, 0));
        Creature second = spawn(EntityType.PIG, new Pos(0, 40, 0));

        ticks(env, 40);

        assertTrue(first.getPosition().distance(second.getPosition()) > 0.3, "pigs are " + first.getPosition().distance(second.getPosition()) + " apart");
    }

    @Test
    void mobsThatDoNotTouchStayPut(Env env) {
        setup(env);
        Creature first = spawn(EntityType.COW, new Pos(0, 40, 0));
        Creature second = spawn(EntityType.COW, new Pos(5, 40, 0));
        double start = first.getPosition().distance(second.getPosition());

        ticks(env, 5);

        assertTrue(Math.abs(first.getPosition().distance(second.getPosition()) - start) < 3);
    }
}
