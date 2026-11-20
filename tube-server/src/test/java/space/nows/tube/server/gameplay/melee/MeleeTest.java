package space.nows.tube.server.gameplay.melee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.server.gameplay.TestHooks;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;
import space.nows.tube.server.mob.Mobs;

import java.lang.reflect.Field;

@EnvTest
class MeleeTest {

    private static final float DIAMOND_SWORD_DAMAGE = 7;

    private Instance instance;
    private Player player;

    private void setup(Env env, ItemStack weapon) {
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        TestHooks.installCombat(MinecraftServer.getGlobalEventHandler());
        TestHooks.installFalling(MinecraftServer.getGlobalEventHandler());
        instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setGameMode(GameMode.SURVIVAL);
        player.setItemInMainHand(weapon);
        for (int i = 0; i < 40; i++) {
            env.tick();
        }
    }

    private Creature spawn(EntityType type, Pos at) {
        Creature creature = new Creature(CreatureKinds.of(type).orElseThrow());
        CreatureKinds.spawn(creature, instance, at).join();
        creature.setInvulnerable(false);
        return creature;
    }

    private static void standOnGround(Entity entity, boolean value) throws ReflectiveOperationException {
        Field field = Entity.class.getDeclaredField("onGround");
        field.setAccessible(true);
        field.setBoolean(entity, value);
    }

    private void fallOnto() {
        EventDispatcher.call(new PlayerMoveEvent(player, new Pos(0, 42, 0), false));
        player.teleport(new Pos(0, 41, 0)).join();
        EventDispatcher.call(new PlayerMoveEvent(player, new Pos(0, 41, 0), false));
    }

    private static float lost(LivingEntity target, Runnable action) {
        float before = target.getHealth();
        action.run();
        return before - target.getHealth();
    }

    private void hit(Entity target) {
        EventDispatcher.call(new EntityAttackEvent(player, target));
    }

    @Test
    void aHitWhileFallingIsCritical(Env env) throws Exception {
        setup(env, ItemStack.of(Material.DIAMOND_SWORD));
        Creature cow = spawn(EntityType.IRON_GOLEM, new Pos(0, 40, 1));
        standOnGround(player, false);
        fallOnto();

        assertTrue(CriticalHit.lands(player, cow, 1));
        assertEquals(DIAMOND_SWORD_DAMAGE * 1.5f, lost(cow, () -> hit(cow)), 0.01);
    }

    @Test
    void aHitOnTheGroundIsNotCritical(Env env) throws Exception {
        setup(env, ItemStack.of(Material.DIAMOND_SWORD));
        Creature cow = spawn(EntityType.IRON_GOLEM, new Pos(0, 40, 1));
        standOnGround(player, true);

        assertFalse(CriticalHit.lands(player, cow, 1));
        assertEquals(DIAMOND_SWORD_DAMAGE, lost(cow, () -> hit(cow)), 0.01);
    }

    @Test
    void sprintingAndSlowSwingsAreNotCritical(Env env) throws Exception {
        setup(env, ItemStack.of(Material.DIAMOND_SWORD));
        Creature cow = spawn(EntityType.COW, new Pos(0, 40, 1));
        standOnGround(player, false);
        fallOnto();

        assertFalse(CriticalHit.lands(player, cow, 0.5));
        player.setSprinting(true);
        assertFalse(CriticalHit.lands(player, cow, 1));
    }

    @Test
    void sharpnessIsNotMultipliedByACritical(Env env) throws Exception {
        ItemStack sword = ItemStack.of(Material.DIAMOND_SWORD).with(DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.SHARPNESS, 2));
        setup(env, sword);
        Creature cow = spawn(EntityType.IRON_GOLEM, new Pos(0, 40, 1));
        standOnGround(player, false);
        fallOnto();

        assertEquals(DIAMOND_SWORD_DAMAGE * 1.5f + 1.5f, lost(cow, () -> hit(cow)), 0.01);
    }

    @Test
    void aSwordSweepsMobsBesideTheTarget(Env env) throws Exception {
        setup(env, ItemStack.of(Material.DIAMOND_SWORD));
        Creature target = spawn(EntityType.COW, new Pos(0, 40, 1.5));
        Creature beside = spawn(EntityType.COW, new Pos(0.8, 40, 1.5));
        Creature far = spawn(EntityType.COW, new Pos(0, 40, 3.9));
        standOnGround(player, true);

        hit(target);

        assertEquals(1, beside.getAttributeValue(Attribute.MAX_HEALTH) - beside.getHealth(), 0.01);
        assertEquals(0, far.getAttributeValue(Attribute.MAX_HEALTH) - far.getHealth(), 0.01);
        assertTrue(target.getAttributeValue(Attribute.MAX_HEALTH) - target.getHealth() >= DIAMOND_SWORD_DAMAGE);
    }

    @Test
    void sweepingEdgeAddsToTheSweep(Env env) throws Exception {
        ItemStack sword = ItemStack.of(Material.DIAMOND_SWORD).with(DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.SWEEPING_EDGE, 3));
        setup(env, sword);
        Creature target = spawn(EntityType.COW, new Pos(0, 40, 1.5));
        Creature beside = spawn(EntityType.COW, new Pos(0.8, 40, 1.5));
        standOnGround(player, true);

        hit(target);

        assertEquals(1 + DIAMOND_SWORD_DAMAGE * 0.75f, beside.getAttributeValue(Attribute.MAX_HEALTH) - beside.getHealth(), 0.01);
    }

    @Test
    void onlySwordsSweep(Env env) throws Exception {
        setup(env, ItemStack.of(Material.DIAMOND_AXE));
        Creature target = spawn(EntityType.COW, new Pos(0, 40, 1.5));
        Creature beside = spawn(EntityType.COW, new Pos(0.8, 40, 1.5));
        standOnGround(player, true);

        hit(target);

        assertEquals(0, beside.getAttributeValue(Attribute.MAX_HEALTH) - beside.getHealth(), 0.01);
    }

    @Test
    void aCriticalDoesNotSweep(Env env) throws Exception {
        setup(env, ItemStack.of(Material.DIAMOND_SWORD));
        Creature target = spawn(EntityType.COW, new Pos(0, 40, 1.5));
        Creature beside = spawn(EntityType.COW, new Pos(0.8, 40, 1.5));
        standOnGround(player, false);
        fallOnto();

        hit(target);

        assertEquals(0, beside.getAttributeValue(Attribute.MAX_HEALTH) - beside.getHealth(), 0.01);
    }

    @Test
    void theSweepNeverHitsTheAttacker(Env env) throws Exception {
        setup(env, ItemStack.of(Material.DIAMOND_SWORD));
        Creature target = spawn(EntityType.COW, new Pos(0, 40, 0.8));
        standOnGround(player, true);
        float health = player.getHealth();

        hit(target);

        assertEquals(health, player.getHealth(), 0.01);
    }
}
