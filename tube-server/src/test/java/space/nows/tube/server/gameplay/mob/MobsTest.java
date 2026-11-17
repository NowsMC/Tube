package space.nows.tube.server.gameplay.mob;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class MobsTest {

    private Instance instance;
    private Player player;

    private void setup(Env env) {
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        CombatAccess.install();
        instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        instance.setTime(18000);
        player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setGameMode(GameMode.SURVIVAL);
    }

    private static void ticks(Env env, int count) {
        for (int i = 0; i < count; i++) {
            env.tick();
        }
    }

    @Test
    void zombiesWalkUpAndHit(Env env) {
        setup(env);
        Mob zombie = new Mob(Mob.Kind.ZOMBIE);
        zombie.setInstance(instance, new Pos(5, 40, 0)).join();
        ticks(env, 200);
        assertTrue(player.getHealth() < 20, () -> "health " + player.getHealth() + " zombie at " + zombie.getPosition());
    }

    @Test
    void armorSoftensHits(Env env) {
        setup(env);
        player.setEquipment(EquipmentSlot.CHESTPLATE, ItemStack.of(Material.DIAMOND_CHESTPLATE));
        player.setEquipment(EquipmentSlot.LEGGINGS, ItemStack.of(Material.DIAMOND_LEGGINGS));
        Mob zombie = new Mob(Mob.Kind.ZOMBIE);
        zombie.setInstance(instance, new Pos(1, 40, 0)).join();
        player.damage(Damage.fromEntity(zombie, 10));
        float taken = 20 - player.getHealth();
        assertTrue(taken < 10 && taken > 0, () -> "took " + taken);
    }

    @Test
    void creepersExplode(Env env) {
        setup(env);
        Mob creeper = new Mob(Mob.Kind.CREEPER);
        creeper.setInstance(instance, new Pos(2, 40, 0)).join();
        ticks(env, 120);
        assertTrue(creeper.isRemoved());
        assertTrue(player.getHealth() < 20);
        assertTrue(instance.getBlock(2, 39, 0).air() || instance.getBlock(1, 39, 0).air());
    }

    @Test
    void cowsDropMeat(Env env) {
        setup(env);
        Mob cow = new Mob(Mob.Kind.COW);
        cow.setInstance(instance, new Pos(10, 40, 10)).join();
        cow.kill();
        ticks(env, 2);
        boolean beef = instance.getEntities().stream().filter(ItemEntity.class::isInstance)
                .anyMatch(entity -> ((ItemEntity) entity).getItemStack().material() == Material.BEEF);
        assertTrue(beef);
    }
}
