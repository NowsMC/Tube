package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.world.Difficulty;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.server.gameplay.Archery;
import space.nows.tube.server.gameplay.TestHooks;
import space.nows.tube.server.mob.trait.SunBurn;

@EnvTest
class MonsterVariantsTest {

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

    private boolean affected(PotionEffect effect) {
        return player.getActiveEffects().stream().anyMatch(active -> active.potion().effect() == effect);
    }

    @Test
    void strayArrowsSlow(Env env) {
        setup(env);
        Creature stray = spawn(EntityType.STRAY, new Pos(8, 40, 0));
        for (int i = 0; i < 400 && !affected(PotionEffect.SLOWNESS); i++) {
            env.tick();
        }
        assertTrue(affected(PotionEffect.SLOWNESS), () -> "health " + player.getHealth() + " stray at " + stray.getPosition());
    }

    @Test
    void husksDoNotBurnInDaylight(Env env) {
        setup(env);
        Creature husk = spawn(EntityType.HUSK, new Pos(0, 40, 0));
        Creature zombie = spawn(EntityType.ZOMBIE, new Pos(4, 40, 0));
        assertTrue(husk.trait(SunBurn.class).isEmpty(), "husk burns");
        assertTrue(zombie.trait(SunBurn.class).isPresent(), "zombie does not burn");
    }

    @Test
    void huskHitsStarve(Env env) {
        setup(env);
        MinecraftServer.setDifficulty(Difficulty.HARD);
        Creature husk = spawn(EntityType.HUSK, new Pos(2, 40, 0));
        for (int i = 0; i < 300 && !affected(PotionEffect.HUNGER); i++) {
            env.tick();
        }
        MinecraftServer.setDifficulty(Difficulty.NORMAL);
        assertTrue(affected(PotionEffect.HUNGER), () -> "health " + player.getHealth() + " husk at " + husk.getPosition());
    }

    @Test
    void caveSpiderBitesPoison(Env env) {
        setup(env);
        MinecraftServer.setDifficulty(Difficulty.NORMAL);
        Creature spider = spawn(EntityType.CAVE_SPIDER, new Pos(2, 40, 0));
        for (int i = 0; i < 300 && !affected(PotionEffect.POISON); i++) {
            env.tick();
        }
        assertTrue(affected(PotionEffect.POISON), () -> "health " + player.getHealth() + " spider at " + spider.getPosition());
    }

    @Test
    void witherSkeletonsWither(Env env) {
        setup(env);
        Creature skeleton = spawn(EntityType.WITHER_SKELETON, new Pos(2, 40, 0));
        for (int i = 0; i < 300 && !affected(PotionEffect.WITHER); i++) {
            env.tick();
        }
        assertTrue(affected(PotionEffect.WITHER), () -> "health " + player.getHealth() + " skeleton at " + skeleton.getPosition());
    }

    @Test
    void zombifiedPiglinsLeavePlayersAloneUntilHit(Env env) {
        setup(env);
        Creature piglin = spawn(EntityType.ZOMBIFIED_PIGLIN, new Pos(3, 40, 0));
        Creature friend = spawn(EntityType.ZOMBIFIED_PIGLIN, new Pos(6, 40, 0));
        ticks(env, 100);
        assertTrue(piglin.target() == null, "attacked without reason");
        piglin.damage(net.minestom.server.entity.damage.Damage.fromPlayer(player, 1));
        ticks(env, 10);
        assertTrue(piglin.target() == player, "did not retaliate");
        assertTrue(friend.target() == player, "friend not alerted");
    }

    @Test
    void llamasSpitBackOnce(Env env) {
        setup(env);
        Creature llama = spawn(EntityType.LLAMA, new Pos(6, 40, 0));
        llama.damage(net.minestom.server.entity.damage.Damage.fromPlayer(player, 1));
        for (int i = 0; i < 200 && player.getHealth() >= 20; i++) {
            env.tick();
        }
        assertTrue(player.getHealth() < 20, "llama did not spit");
        ticks(env, 10);
        assertTrue(llama.target() == null, "llama kept its target after spitting");
    }

    @Test
    void slimesHopAndSplit(Env env) {
        setup(env);
        Creature slime = spawn(EntityType.SLIME, new Pos(6, 40, 6));
        Pos start = slime.getPosition();
        ticks(env, 200);
        assertTrue(slime.getPosition().distance(start) > 1, "slime never moved");
        if (slime.getEntityMeta() instanceof net.minestom.server.entity.metadata.cube.AbstractCubeMeta meta) {
            meta.setSize(2);
        }
        slime.kill();
        ticks(env, 5);
        long alive = instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.SLIME && !((Creature) entity).isDead()).count();
        assertTrue(alive >= 2, () -> "split into " + alive);
    }
}
