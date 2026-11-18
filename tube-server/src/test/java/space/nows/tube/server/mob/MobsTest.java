package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.server.gameplay.Archery;
import space.nows.tube.server.gameplay.TestHooks;
import space.nows.tube.server.mob.life.Lifecycle;
import space.nows.tube.server.mob.loot.MobLoot;

@EnvTest
class MobsTest {

    private Instance instance;
    private Player player;

    private void setup(Env env) {
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        TestHooks.installCombat(MinecraftServer.getGlobalEventHandler());
        Archery.install(MinecraftServer.getGlobalEventHandler());
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
    void zombiesWalkUpAndHit(Env env) {
        setup(env);
        Creature zombie = spawn(EntityType.ZOMBIE, new Pos(5, 40, 0));
        ticks(env, 200);
        assertTrue(player.getHealth() < 20, () -> "health " + player.getHealth() + " zombie at " + zombie.getPosition());
    }

    @Test
    void skeletonsShootFromRange(Env env) {
        setup(env);
        Creature skeleton = spawn(EntityType.SKELETON, new Pos(8, 40, 0));
        ticks(env, 200);
        assertTrue(player.getHealth() < 20, () -> "health " + player.getHealth() + " skeleton at " + skeleton.getPosition());
    }

    @Test
    void armorSoftensHits(Env env) {
        setup(env);
        player.setEquipment(EquipmentSlot.CHESTPLATE, ItemStack.of(Material.DIAMOND_CHESTPLATE));
        player.setEquipment(EquipmentSlot.LEGGINGS, ItemStack.of(Material.DIAMOND_LEGGINGS));
        Creature zombie = spawn(EntityType.ZOMBIE, new Pos(1, 40, 0));
        player.damage(Damage.fromEntity(zombie, 10));
        float taken = 20 - player.getHealth();
        assertTrue(taken < 10 && taken > 0, () -> "took " + taken);
    }

    @Test
    void creepersExplode(Env env) {
        setup(env);
        Creature creeper = spawn(EntityType.CREEPER, new Pos(2, 40, 0));
        ticks(env, 120);
        assertTrue(creeper.isRemoved());
        assertTrue(player.getHealth() < 20);
        var center = creeper.getPosition();
        int holes = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (instance.getBlock(center.blockX() + dx, 39, center.blockZ() + dz).air()) {
                    holes++;
                }
            }
        }
        assertTrue(holes > 0);
    }

    @Test
    void cowsDropMeat(Env env) {
        setup(env);
        Creature cow = spawn(EntityType.COW, new Pos(10, 40, 10));
        boolean beef = MobLoot.of(cow).items().stream().anyMatch(item -> item.material() == Material.BEEF);
        assertTrue(beef);
    }

    @Test
    void cowsInLoveHaveACalfThatGrowsUp(Env env) {
        setup(env);
        Creature mother = spawn(EntityType.COW, new Pos(10, 40, 10));
        Creature father = spawn(EntityType.COW, new Pos(12, 40, 10));
        Lifecycle motherLife = mother.trait(Lifecycle.class).orElseThrow();
        Lifecycle fatherLife = father.trait(Lifecycle.class).orElseThrow();
        motherLife.fallInLove(player);
        fatherLife.fallInLove(player);
        ticks(env, 300);
        Creature calf = instance.getEntities().stream()
                .filter(entity -> entity instanceof Creature creature && creature.getEntityType() == EntityType.COW && creature.isBaby())
                .map(Creature.class::cast).findFirst().orElse(null);
        assertTrue(calf != null, "no calf");
        assertTrue(motherLife.age() > 0 && fatherLife.age() > 0, "parents not on cooldown");
        Lifecycle calfLife = calf.trait(Lifecycle.class).orElseThrow();
        calfLife.setAge(-1);
        ticks(env, 2);
        assertTrue(!calf.isBaby(), "calf did not grow up");
    }
}
