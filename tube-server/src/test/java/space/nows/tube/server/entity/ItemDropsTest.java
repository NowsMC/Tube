package space.nows.tube.server.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.event.entity.EntityPickupItemEvent;
import space.nows.tube.api.event.player.PlayerDropItemEvent;
import space.nows.tube.api.event.player.PlayerRespawnEvent;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.event.DefaultEventBus;
import space.nows.tube.server.event.EventBridge;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@EnvTest
class ItemDropsTest {

    @TempDir
    Path directory;

    private final DefaultEventBus bus = new DefaultEventBus();
    private WorldService worlds;
    private MinestomWorld world;

    private Player join(Env env) {
        worlds = new WorldService(directory, "main");
        PlayerService players = new PlayerService(worlds, new PermissionService(directory));
        new EventBridge(bus, players, worlds).install(MinecraftServer.getGlobalEventHandler());
        new ItemDrops(bus, players, worlds).install(MinecraftServer.getGlobalEventHandler());
        world = (MinestomWorld) worlds.defaultWorld();
        return env.createPlayer(world.instance(), new Pos(0, 42, 0));
    }

    private List<ItemEntity> droppedItems() {
        return world.instance().getEntities().stream().filter(ItemEntity.class::isInstance).map(ItemEntity.class::cast).toList();
    }

    @Test
    void droppedItemsBecomeEntitiesInTheWorld(Env env) {
        Player player = join(env);
        List<String> drops = new ArrayList<>();
        bus.subscribe(PlayerDropItemEvent.class, event -> drops.add(event.item().item().type() + "x" + event.item().item().amount()));

        assertTrue(player.dropItem(net.minestom.server.item.ItemStack.of(net.minestom.server.item.Material.DIAMOND, 3)));

        assertEquals(List.of("minecraft:diamondx3"), drops);
        assertEquals(1, droppedItems().size());
        assertEquals(3, droppedItems().getFirst().getItemStack().amount());
    }

    @Test
    void cancelledDropsLeaveNoEntity(Env env) {
        Player player = join(env);
        bus.subscribe(PlayerDropItemEvent.class, event -> event.setCancelled(true));

        assertFalse(player.dropItem(net.minestom.server.item.ItemStack.of(net.minestom.server.item.Material.DIAMOND)));

        env.tick();
        assertTrue(droppedItems().isEmpty());
    }

    @Test
    void pickedUpItemsGoIntoTheInventory(Env env) {
        Player player = join(env);
        ItemEntity item = new ItemEntity(net.minestom.server.item.ItemStack.of(net.minestom.server.item.Material.EMERALD, 2));
        item.setInstance(world.instance(), new Pos(0, 42, 0)).join();

        PickupItemEvent pickup = new PickupItemEvent(player, item);
        EventDispatcher.call(pickup);

        assertFalse(pickup.isCancelled());
        assertEquals(2, player.getInventory().getItemStack(0).amount());
    }

    @Test
    void pickupsCanBeCancelled(Env env) {
        Player player = join(env);
        bus.subscribe(EntityPickupItemEvent.class, event -> event.setCancelled(true));
        ItemEntity item = new ItemEntity(net.minestom.server.item.ItemStack.of(net.minestom.server.item.Material.EMERALD));
        item.setInstance(world.instance(), new Pos(0, 42, 0)).join();

        PickupItemEvent pickup = new PickupItemEvent(player, item);
        EventDispatcher.call(pickup);

        assertTrue(pickup.isCancelled());
        assertTrue(player.getInventory().getItemStack(0).isAir());
    }

    @Test
    void respawnLocationCanBeChanged(Env env) {
        Player player = join(env);
        bus.subscribe(PlayerRespawnEvent.class, event -> event.setLocation(new Location(event.player().world(), 5, 50, 5)));

        var respawn = new net.minestom.server.event.player.PlayerRespawnEvent(player);
        EventDispatcher.call(respawn);

        assertEquals(new Pos(5, 50, 5), respawn.getRespawnPosition());
    }
}
