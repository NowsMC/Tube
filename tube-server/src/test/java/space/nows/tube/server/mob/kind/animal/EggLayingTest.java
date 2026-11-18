package space.nows.tube.server.mob.kind.animal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;
import space.nows.tube.server.mob.Mobs;

import java.util.List;

@EnvTest
class EggLayingTest {

    @Test
    void aChickenEventuallyLaysAnEgg(Env env) {
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        Instance instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        env.createPlayer(instance, new Pos(4, 40, 4));
        Creature chicken = new Creature(CreatureKinds.of(EntityType.CHICKEN).orElseThrow());
        CreatureKinds.spawn(chicken, instance, new Pos(0, 40, 0)).join();

        for (int i = 0; i < 12100; i++) {
            env.tick();
        }

        List<ItemEntity> eggs = instance.getEntities().stream().filter(entity -> entity instanceof ItemEntity item && item.getItemStack().material() == Material.EGG).map(ItemEntity.class::cast).toList();
        assertTrue(!eggs.isEmpty(), "the chicken laid no egg in 12100 ticks, alive " + !chicken.isDead());
    }
}
