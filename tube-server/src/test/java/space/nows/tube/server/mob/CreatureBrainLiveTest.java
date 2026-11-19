package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.api.entity.mob.brain.MemoryKey;
import space.nows.tube.api.entity.mob.brain.Sense;

import java.util.Optional;

@EnvTest
class CreatureBrainLiveTest {

    private static final MemoryKey<Long> HEARD = MemoryKey.of("test:heard", Long.class);

    private static final class WalkEast extends Behavior {

        private final Creature creature;
        int started;

        WalkEast(Creature creature) {
            super(Control.MOVE);
            this.creature = creature;
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public void start() {
            started++;
            creature.navigation().moveTo(new Vec(12, 40, 0), 1.0);
        }
    }

    private Creature spawn(Env env, EntityType type) {
        Instance instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        Creature creature = new Creature(CreatureKinds.of(type).orElseThrow());
        CreatureKinds.spawn(creature, instance, new Pos(0, 40, 0)).join();
        return creature;
    }

    @Test
    void addedBehaviorOutranksTheKindsOwn(Env env) {
        Creature cow = spawn(env, EntityType.COW);
        WalkEast walk = new WalkEast(cow);
        cow.brain().add(-1, walk);
        for (int i = 0; i < 200; i++) {
            env.tick();
        }
        assertEquals(1, walk.started);
        assertTrue(cow.brain().running(walk));
        assertTrue(cow.getPosition().x() > 4, () -> "cow at " + cow.getPosition());
    }

    @Test
    void sensesFillMemories(Env env) {
        Creature zombie = spawn(env, EntityType.ZOMBIE);
        zombie.brain().addSense(new Sense() {
            @Override
            public int interval() {
                return 10;
            }

            @Override
            public void sense(Brain brain) {
                brain.remember(HEARD, zombie.getAliveTicks(), 15);
            }
        });
        for (int i = 0; i < 40; i++) {
            env.tick();
        }
        Optional<Long> heard = zombie.brain().recall(HEARD);
        assertTrue(heard.isPresent());
        assertTrue(zombie.getAliveTicks() - heard.get() <= 10);
    }
}
