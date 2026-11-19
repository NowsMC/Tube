package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class DespawnTest {

    private Instance instance;

    private void setup(Env env) {
        instance = env.createFlatInstance();
        for (int x = -2; x <= 12; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
    }

    private Creature spawn(EntityType type, Pos at) {
        Creature creature = new Creature(CreatureKinds.of(type).orElseThrow());
        creature.setInstance(instance, at).join();
        return creature;
    }

    private static void ticks(Env env, int count) {
        for (int i = 0; i < count; i++) {
            env.tick();
        }
    }

    @Test
    void nobodyLeavesAnEmptyWorld(Env env) {
        setup(env);
        Creature zombie = spawn(EntityType.ZOMBIE, new Pos(0, 40, 0));
        ticks(env, 20);
        assertFalse(zombie.isRemoved());
    }

    @Test
    void farMonstersLeaveButAnimalsStay(Env env) {
        setup(env);
        env.createPlayer(instance, new Pos(0, 40, 0));
        Creature zombie = spawn(EntityType.ZOMBIE, new Pos(150, 40, 0));
        Creature cow = spawn(EntityType.COW, new Pos(160, 40, 0));
        Creature kept = spawn(EntityType.ZOMBIE, new Pos(170, 40, 0));
        kept.setPersistent(true);
        ticks(env, 5);
        assertTrue(zombie.isRemoved());
        assertFalse(cow.isRemoved());
        assertFalse(kept.isRemoved());
    }
}
