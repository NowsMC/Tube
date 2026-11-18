package space.nows.tube.server.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@EnvTest
class ArcheryTest {

    private Instance instance;
    private Player player;
    private Collector<SpawnEntityPacket> spawns;
    private Collector<EntityVelocityPacket> velocities;

    private void join(Env env, GameMode mode) {
        Archery.install(MinecraftServer.getGlobalEventHandler());
        instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var connection = env.createConnection(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000d4"), "Archer"));
        player = connection.connect(instance, new Pos(8, 40, 8, 0, 0));
        spawns = connection.trackIncoming(SpawnEntityPacket.class);
        velocities = connection.trackIncoming(EntityVelocityPacket.class);
        player.setGameMode(mode);
        player.setItemInMainHand(ItemStack.of(Material.BOW));
        player.getInventory().setItemStack(9, ItemStack.of(Material.ARROW, 4));
    }

    private void release(long useTicks) {
        EventDispatcher.call(new PlayerCancelItemUseEvent(player, PlayerHand.MAIN, player.getItemInMainHand(), useTicks));
    }

    private List<Entity> arrows() {
        return instance.getEntities().stream().filter(entity -> entity.getEntityType() == EntityType.ARROW).toList();
    }

    @Test
    void aFullyDrawnArrowFliesAwayFromThePlayer(Env env) {
        join(env, GameMode.SURVIVAL);

        release(20);
        env.tick();
        Entity arrow = arrows().getFirst();
        double start = arrow.getPosition().z();
        for (int i = 0; i < 10; i++) {
            env.tick();
        }

        assertTrue(arrow.getPosition().z() - start > 10, "arrow moved " + (arrow.getPosition().z() - start));
        assertEquals(3, player.getInventory().getItemStack(9).amount());
    }

    @Test
    void theClientIsToldWhereTheArrowGoesWhenItAppears(Env env) {
        join(env, GameMode.SURVIVAL);

        release(20);
        env.tick();

        Entity arrow = arrows().getFirst();
        Vec spawned = spawns.collect().stream().filter(packet -> packet.entityId() == arrow.getEntityId()).findFirst().orElseThrow().velocity();
        boolean separate = velocities.collect().stream().anyMatch(packet -> packet.entityId() == arrow.getEntityId() && packet.velocity().length() > 1);
        assertTrue(spawned.length() > 1 || separate, "the client never learned the arrow velocity, spawn velocity " + spawned);
    }

    @Test
    void anArrowShotLevelStartsLevel(Env env) {
        join(env, GameMode.SURVIVAL);

        release(20);
        env.tick();

        Entity arrow = arrows().getFirst();
        assertTrue(Math.abs(arrow.getVelocity().y()) < 3, "arrow velocity " + arrow.getVelocity());
        assertTrue(arrow.getVelocity().z() > 40, "arrow velocity " + arrow.getVelocity());
    }

    @Test
    void aShortDrawShootsNothing(Env env) {
        join(env, GameMode.SURVIVAL);

        release(1);
        env.tick();

        assertTrue(arrows().isEmpty());
        assertEquals(4, player.getInventory().getItemStack(9).amount());
    }

    @Test
    void creativeShootsWithoutUsingArrows(Env env) {
        join(env, GameMode.CREATIVE);

        release(20);
        env.tick();

        assertEquals(1, arrows().size());
        assertEquals(4, player.getInventory().getItemStack(9).amount());
    }
}
