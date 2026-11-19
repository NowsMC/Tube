package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.server.gameplay.TestHooks;

import java.util.ArrayList;
import java.util.List;

@EnvTest
class AllKindsTest {

    @Test
    void everyKindSpawnsAndThinks(Env env) {
        TestHooks.installCombat(MinecraftServer.getGlobalEventHandler());
        Instance instance = env.createFlatInstance();
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        env.createPlayer(instance, new Pos(0, 40, 0)).setGameMode(GameMode.SURVIVAL);
        List<Creature> creatures = new ArrayList<>();
        int slot = 0;
        for (CreatureKind kind : CreatureKinds.all()) {
            Creature creature = new Creature(kind);
            CreatureKinds.spawn(creature, instance, new Pos((slot % 10) * 6 - 30, 40, (slot / 10) * 6 - 30)).join();
            creatures.add(creature);
            slot++;
        }
        for (int i = 0; i < 100; i++) {
            env.tick();
        }
        for (Creature creature : creatures) {
            assertTrue(creature.getAliveTicks() > 0 || creature.isRemoved(), () -> creature.kind().type() + " never ticked");
        }
    }
}
