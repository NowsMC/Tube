package space.nows.tube.server.gameplay.throwing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;
import space.nows.tube.server.mob.Mobs;

import java.util.List;
import java.util.random.RandomGenerator;

@EnvTest
class ThrowingTest {

    private Instance instance;
    private Player player;

    private void join(Env env, GameMode mode, float pitch) {
        Throwing.install(MinecraftServer.getGlobalEventHandler());
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        player = env.createPlayer(instance, new Pos(0, 40, 0, 0, pitch));
        player.setGameMode(mode);
    }

    private void use(Material material, int amount) {
        player.setItemInMainHand(ItemStack.of(material, amount));
        EventDispatcher.call(new PlayerUseItemEvent(player, PlayerHand.MAIN, player.getItemInMainHand(), 0));
    }

    private List<Entity> thrown(EntityType type) {
        return instance.getEntities().stream().filter(entity -> entity.getEntityType() == type).toList();
    }

    private static void ticks(Env env, int count) {
        for (int i = 0; i < count; i++) {
            env.tick();
        }
    }

    @Test
    void aSnowballFliesAndUsesUpOneItem(Env env) {
        join(env, GameMode.SURVIVAL, 0);

        use(Material.SNOWBALL, 16);
        env.tick();
        Entity snowball = thrown(EntityType.SNOWBALL).getFirst();
        double start = snowball.getPosition().z();
        ticks(env, 5);

        assertTrue(snowball.getPosition().z() - start > 5, "snowball moved " + (snowball.getPosition().z() - start));
        assertEquals(15, player.getItemInMainHand().amount());
    }

    @Test
    void creativeThrowsWithoutUsingItems(Env env) {
        join(env, GameMode.CREATIVE, 0);

        use(Material.SNOWBALL, 16);

        assertEquals(16, player.getItemInMainHand().amount());
        assertEquals(1, thrown(EntityType.SNOWBALL).size());
    }

    @Test
    void eggsAreThrownToo(Env env) {
        join(env, GameMode.SURVIVAL, 0);

        use(Material.BROWN_EGG, 2);

        assertEquals(1, thrown(EntityType.EGG).size());
    }

    @Test
    void aSnowballHitsAMobAndDisappears(Env env) {
        join(env, GameMode.SURVIVAL, 0);
        Creature cow = new Creature(CreatureKinds.of(EntityType.COW).orElseThrow());
        CreatureKinds.spawn(cow, instance, new Pos(0, 40, 6)).join();

        use(Material.SNOWBALL, 4);
        ticks(env, 12);

        assertTrue(thrown(EntityType.SNOWBALL).isEmpty(), "the snowball is still there");
    }

    @Test
    void anEnderPearlMovesThePlayerAndHurtsThem(Env env) {
        join(env, GameMode.SURVIVAL, 90);
        player.teleport(new Pos(0, 50, 0, 0, 90)).join();
        ticks(env, 2);
        float health = player.getHealth();

        use(Material.ENDER_PEARL, 2);
        ticks(env, 40);

        assertTrue(player.getPosition().y() < 45, "player is at " + player.getPosition());
        assertEquals(health - 5, player.getHealth(), 0.01);
        assertEquals(1, player.getItemInMainHand().amount());
    }

    @Test
    void aSecondPearlWaitsForTheCooldown(Env env) {
        join(env, GameMode.CREATIVE, 90);

        use(Material.ENDER_PEARL, 2);
        use(Material.ENDER_PEARL, 2);

        assertEquals(1, thrown(EntityType.ENDER_PEARL).size());
    }

    @Test
    void anEggHatchesAChickWhenTheDiceAllow(Env env) {
        join(env, GameMode.SURVIVAL, 0);
        RandomGenerator lucky = new RandomGenerator() {
            @Override
            public long nextLong() {
                return 0;
            }

            @Override
            public int nextInt(int bound) {
                return 1 == bound ? 0 : bound > 8 ? 1 : 0;
            }
        };

        int hatched = EggHatching.hatch(instance, new Pos(3, 40, 3), lucky);

        assertEquals(1, hatched);
        assertEquals(1, thrown(EntityType.CHICKEN).size());
    }

    @Test
    void anEggUsuallyDoesNotHatch(Env env) {
        join(env, GameMode.SURVIVAL, 0);
        RandomGenerator unlucky = new RandomGenerator() {
            @Override
            public long nextLong() {
                return 0;
            }

            @Override
            public int nextInt(int bound) {
                return bound - 1;
            }
        };

        assertEquals(0, EggHatching.hatch(instance, new Pos(3, 40, 3), unlucky));
        assertFalse(thrown(EntityType.CHICKEN).iterator().hasNext());
    }
}
