package space.nows.tube.server.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.inventory.click.Click;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.EquipmentSlot;
import space.nows.tube.api.Inventory;
import space.nows.tube.api.ItemStack;
import space.nows.tube.api.LivingEntity;
import space.nows.tube.api.Location;
import space.nows.tube.api.Text;
import space.nows.tube.api.event.EntityDamageEvent;
import space.nows.tube.api.event.EntityDeathEvent;
import space.nows.tube.api.event.EntitySpawnEvent;
import space.nows.tube.api.event.InventoryClickEvent;
import space.nows.tube.server.DedicatedServer;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.event.DefaultEventBus;
import space.nows.tube.server.event.EventBridge;
import space.nows.tube.server.inventory.MinestomInventory;
import space.nows.tube.server.player.MinestomPlayer;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@EnvTest
class ItemsAndEntitiesTest {

    @TempDir
    Path directory;

    private final DefaultEventBus bus = new DefaultEventBus();
    private WorldService worlds;
    private PlayerService players;

    private MinestomPlayer join(Env env) {
        worlds = new WorldService(directory, "main");
        players = new PlayerService(worlds, new PermissionService(directory));
        new EventBridge(bus, players, worlds).install(MinecraftServer.getGlobalEventHandler());
        return players.wrap(env.createPlayer(((MinestomWorld) worlds.defaultWorld()).instance(), new Pos(0, 42, 0)));
    }

    @Test
    void itemsKeepTheirNameAndLoreThroughAnInventory(Env env) {
        MinestomPlayer player = join(env);
        ItemStack sword = ItemStack.of("diamond_sword", 1)
                .withName(Text.mini("<red>Blade"))
                .withLore(List.of(Text.plain("sharp")));

        assertTrue(player.inventory().add(sword));

        ItemStack found = player.inventory().item(0);
        assertEquals("minecraft:diamond_sword", found.type());
        assertEquals("Blade", PlainTextComponentSerializer.plainText().serialize(found.name()));
        assertEquals("sharp", PlainTextComponentSerializer.plainText().serialize(found.lore().getFirst()));
        assertTrue(player.inventory().item(5).isAir());

        assertThrows(IllegalArgumentException.class, () -> player.inventory().setItem(1, ItemStack.of("tube:nothing")));
    }

    @Test
    void equipmentAndHeldSlotWork(Env env) {
        MinestomPlayer player = join(env);

        player.setEquipment(EquipmentSlot.HEAD, ItemStack.of("iron_helmet"));
        player.inventory().setItem(3, ItemStack.of("apple", 4));
        player.setHeldSlot(3);

        assertEquals("minecraft:iron_helmet", player.equipment(EquipmentSlot.HEAD).type());
        assertEquals(3, player.heldSlot());
        assertEquals(4, player.equipment(EquipmentSlot.MAIN_HAND).amount());
        assertThrows(IllegalArgumentException.class, () -> player.setHeldSlot(9));
    }

    @Test
    void potionEffectsAreAppliedAndRemoved(Env env) {
        MinestomPlayer player = join(env);

        player.addEffect(space.nows.tube.api.PotionEffect.of("speed", 200, 1));

        assertEquals(1, player.effects().size());
        assertEquals("minecraft:speed", player.effects().getFirst().type());
        assertEquals(1, player.effects().getFirst().amplifier());
        player.removeEffect("speed");
        assertTrue(player.effects().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> player.addEffect(space.nows.tube.api.PotionEffect.of("tube:nothing", 20, 0)));
    }

    @Test
    void chunksCanBeLoadedAndUnloaded(Env env) {
        MinestomPlayer player = join(env);
        var world = worlds.defaultWorld();

        world.loadChunk(40, 41).join();
        assertTrue(world.isChunkLoaded(40, 41));
        assertTrue(world.loadedChunks().contains(new space.nows.tube.api.ChunkPos(40, 41)));

        world.unloadChunk(40, 41);
        assertFalse(world.isChunkLoaded(40, 41));
    }

    @Test
    void cancelledTeleportsLeaveThePlayerInPlace(Env env) {
        MinestomPlayer player = join(env);
        players.attachEvents(bus);
        bus.subscribe(space.nows.tube.api.event.PlayerTeleportEvent.class, event -> event.setCancelled(true));
        double before = player.location().x();

        player.teleport(new Location(player.world(), before + 50, 42, 0)).join();

        assertEquals(before, player.location().x());
    }

    @Test
    void containersOpenForPlayers(Env env) {
        MinestomPlayer player = join(env);
        Inventory menu = new DedicatedServer(() -> "26.2", directory).createInventory(Component.text("Menu"), 3);

        assertEquals(27, menu.size());
        menu.setItem(4, ItemStack.of("emerald", 2));
        player.openInventory(menu);

        assertSame(((MinestomInventory) menu).handle(), ((net.minestom.server.entity.Player) playerHandle(player)).getOpenInventory());
        assertThrows(IllegalArgumentException.class, () -> new DedicatedServer(() -> "26.2", directory).createInventory(Component.text("x"), 7));
        assertThrows(IllegalArgumentException.class, () -> player.openInventory(player.inventory()));
    }

    @Test
    void clicksCanBeCancelled(Env env) {
        MinestomPlayer player = join(env);
        Inventory menu = new DedicatedServer(() -> "26.2", directory).createInventory(Component.text("Menu"), 1);
        menu.setItem(2, ItemStack.of("emerald"));
        player.openInventory(menu);
        List<String> clicks = new ArrayList<>();
        bus.subscribe(InventoryClickEvent.class, event -> {
            clicks.add(event.clicked().type() + "@" + event.slot());
            event.setCancelled(true);
        });

        var click = new net.minestom.server.event.inventory.InventoryPreClickEvent(
                ((MinestomInventory) menu).handle(), (net.minestom.server.entity.Player) playerHandle(player), new Click.Left(2));
        EventDispatcher.call(click);

        assertEquals(List.of("minecraft:emerald@2"), clicks);
        assertTrue(click.isCancelled());
    }

    @Test
    void worldsSpawnLivingAndPlainEntities(Env env) {
        MinestomPlayer player = join(env);
        var world = player.world();
        Location spot = new Location(world, 4, 42, 4);

        var zombie = world.spawn("zombie", spot);
        var arrow = world.spawn("minecraft:arrow", spot);
        var dropped = world.dropItem(spot, ItemStack.of("diamond", 3));

        assertEquals("minecraft:zombie", zombie.type());
        assertInstanceOf(LivingEntity.class, zombie);
        assertFalse(arrow instanceof LivingEntity);
        assertEquals("minecraft:item", dropped.type());
        assertTrue(((LivingEntity) zombie).maxHealth() > 0);
        assertTrue(world.entities().contains(zombie));
        assertEquals(4.0, zombie.location().x());

        zombie.setCustomName(Text.mini("<gold>Bob"));
        assertEquals("Bob", PlainTextComponentSerializer.plainText().serialize(zombie.customName().orElseThrow()));

        zombie.remove();
        assertTrue(zombie.isRemoved());
        assertThrows(IllegalArgumentException.class, () -> world.spawn("tube:unknown", spot));
        assertThrows(IllegalArgumentException.class, () -> world.spawn("player", spot));
    }

    @Test
    void entityEventsReachPlugins(Env env) {
        MinestomPlayer player = join(env);
        var world = player.world();
        List<String> log = new ArrayList<>();
        bus.subscribe(EntitySpawnEvent.class, event -> log.add("spawn:" + event.entity().type()));
        bus.subscribe(EntityDamageEvent.class, event -> {
            log.add("damage:" + event.damage());
            event.setCancelled(true);
        });
        bus.subscribe(EntityDeathEvent.class, event -> log.add("death:" + event.entity().type()));

        var zombie = (LivingEntity) world.spawn("zombie", new Location(world, 4, 42, 4));
        ((MinestomLivingEntity) zombie).minestom().damage(DamageType.GENERIC, 4f);
        assertEquals(zombie.maxHealth(), zombie.health());

        bus.subscribe(EntityDamageEvent.class, space.nows.tube.api.event.EventPriority.HIGHEST, false, event -> event.setCancelled(false));
        ((MinestomLivingEntity) zombie).minestom().damage(DamageType.GENERIC, 1000f);

        assertEquals(List.of("spawn:minecraft:zombie", "damage:4.0", "damage:1000.0", "death:minecraft:zombie"), log);
    }

    private static Object playerHandle(MinestomPlayer player) {
        return player.minestom();
    }
}
