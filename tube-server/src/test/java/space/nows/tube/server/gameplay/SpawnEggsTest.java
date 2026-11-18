package space.nows.tube.server.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.BlockPlacementListener;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.Mobs;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.List;

@EnvTest
class SpawnEggsTest {

    @TempDir
    Path directory;

    private Instance instance;
    private Player player;

    private void join(Env env, GameMode mode) {
        WorldService worlds = new WorldService(directory, "main");
        instance = ((space.nows.tube.server.world.MinestomWorld) worlds.defaultWorld()).instance();
        instance.loadChunk(0, 0).join();
        new SpawnEggs(worlds).install(MinecraftServer.getGlobalEventHandler());
        Mobs.install(MinecraftServer.getGlobalEventHandler());
        player = env.createPlayer(instance, new Pos(8, 42, 8));
        player.setGameMode(mode);
    }

    private void useOnTop(Material egg, int amount) {
        instance.setBlock(8, 40, 8, Block.STONE);
        player.setItemInMainHand(ItemStack.of(egg, amount));
        BlockPlacementListener.listener(new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, new Vec(8, 40, 8), BlockFace.TOP, 0.5f, 1f, 0.5f, false, false, 0), player);
    }

    private List<Entity> of(EntityType type) {
        return instance.getEntities().stream().filter(entity -> entity.getEntityType() == type).toList();
    }

    @Test
    void anEggSpawnsItsMobWithItsBrain(Env env) {
        join(env, GameMode.SURVIVAL);

        useOnTop(Material.COW_SPAWN_EGG, 3);

        assertEquals(1, of(EntityType.COW).size());
        assertTrue(of(EntityType.COW).getFirst() instanceof Creature, "the cow has no Tube brain");
        assertEquals(2, player.getItemInMainHand().amount());
    }

    @Test
    void creativeKeepsTheEgg(Env env) {
        join(env, GameMode.CREATIVE);

        useOnTop(Material.COD_SPAWN_EGG, 1);

        assertEquals(1, of(EntityType.COD).size());
        assertEquals(1, player.getItemInMainHand().amount());
    }

    @Test
    void mobsWithoutATubeKindStillSpawn(Env env) {
        join(env, GameMode.CREATIVE);

        useOnTop(Material.ALLAY_SPAWN_EGG, 1);

        assertEquals(1, of(EntityType.ALLAY).size());
    }

    @Test
    void otherItemsDoNothing(Env env) {
        join(env, GameMode.SURVIVAL);

        useOnTop(Material.STICK, 1);

        assertTrue(instance.getEntities().stream().noneMatch(entity -> entity.getEntityType() == EntityType.COW));
        assertNull(SpawnEggs.entity(ItemStack.of(Material.STICK)));
        assertEquals(EntityType.ZOMBIE, SpawnEggs.entity(ItemStack.of(Material.ZOMBIE_SPAWN_EGG)));
    }
}
