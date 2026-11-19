package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.server.gameplay.TestHooks;
import space.nows.tube.server.mob.life.Tameness;

@EnvTest
class PetsTest {

    @Test
    void wolvesTameFollowAndDefend(Env env) {
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        TestHooks.installCombat(MinecraftServer.getGlobalEventHandler());
        Instance instance = env.createFlatInstance();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        Player player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setGameMode(GameMode.SURVIVAL);
        Creature wolf = new Creature(CreatureKinds.of(EntityType.WOLF).orElseThrow());
        wolf.setInstance(instance, new Pos(2, 40, 0)).join();
        Tameness tameness = wolf.trait(Tameness.class).orElseThrow();
        for (int i = 0; i < 64 && !tameness.tamed(); i++) {
            player.setItemInMainHand(ItemStack.of(Material.BONE));
            MinecraftServer.getGlobalEventHandler().call(new PlayerEntityInteractEvent(player, wolf, PlayerHand.MAIN, wolf.getPosition()));
        }
        assertTrue(tameness.tamed(), "wolf never tamed");
        assertTrue(tameness.sitting(), "tamed wolf should sit");
        player.setItemInMainHand(ItemStack.AIR);
        MinecraftServer.getGlobalEventHandler().call(new PlayerEntityInteractEvent(player, wolf, PlayerHand.MAIN, wolf.getPosition()));
        assertTrue(!tameness.sitting(), "wolf did not stand");
        player.teleport(new Pos(20, 40, 0)).join();
        for (int i = 0; i < 200; i++) {
            env.tick();
        }
        assertTrue(wolf.getPosition().distance(player.getPosition()) < 10, () -> "wolf at " + wolf.getPosition());
        Creature zombie = new Creature(CreatureKinds.of(EntityType.ZOMBIE).orElseThrow());
        zombie.setInstance(instance, new Pos(24, 40, 0)).join();
        space.nows.tube.server.mob.sense.Assault.record(zombie, player);
        for (int i = 0; i < 10; i++) {
            env.tick();
        }
        assertTrue(wolf.target() == zombie, "wolf did not defend its owner");
    }
}
