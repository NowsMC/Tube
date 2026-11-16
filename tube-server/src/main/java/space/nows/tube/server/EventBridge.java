package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import space.nows.tube.api.event.BlockBreakEvent;
import space.nows.tube.api.LivingEntity;
import space.nows.tube.api.event.BlockPlaceEvent;
import space.nows.tube.api.event.EntityDamageEvent;
import space.nows.tube.api.event.EntityDeathEvent;
import space.nows.tube.api.event.EntitySpawnEvent;
import space.nows.tube.api.event.InventoryClickEvent;
import space.nows.tube.api.event.InventoryCloseEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerQuitEvent;

final class EventBridge {

    private final EventBus bus;
    private final PlayerService players;
    private final WorldService worlds;

    EventBridge(EventBus bus, PlayerService players, WorldService worlds) {
        this.bus = bus;
        this.players = players;
        this.worlds = worlds;
    }

    private static void announce(Component message) {
        if (message != null) {
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(message));
        }
    }

    void install(EventNode<net.minestom.server.event.Event> node) {
        node.addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                var joined = bus.post(new PlayerJoinEvent(players.wrap(event.getPlayer())));
                announce(joined.joinMessage());
            }
        });

        node.addListener(PlayerDisconnectEvent.class, event -> {
            var left = bus.post(new PlayerQuitEvent(players.wrap(event.getPlayer())));
            players.remove(event.getPlayer());
            announce(left.quitMessage());
        });

        node.addListener(PlayerChatEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var posted = bus.post(new space.nows.tube.api.event.PlayerChatEvent(player, event.getRawMessage()));
            if (posted.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            if (!posted.message().equals(event.getRawMessage())) {
                event.setFormattedMessage(Component.text("<" + player.name() + "> " + posted.message()));
            }
        });

        node.addListener(PlayerBlockBreakEvent.class, event -> {
            var pos = event.getBlockPosition();
            var posted = bus.post(new BlockBreakEvent(
                    players.wrap(event.getPlayer()), worlds.of(event.getInstance()),
                    pos.blockX(), pos.blockY(), pos.blockZ(), BlockMapper.fromMinestom(event.getBlock())));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerBlockPlaceEvent.class, event -> {
            var pos = event.getBlockPosition();
            var posted = bus.post(new BlockPlaceEvent(
                    players.wrap(event.getPlayer()), worlds.of(event.getInstance()),
                    pos.blockX(), pos.blockY(), pos.blockZ(), BlockMapper.fromMinestom(event.getBlock())));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            } else if (!posted.block().equals(BlockMapper.fromMinestom(event.getBlock()))) {
                event.setBlock(BlockMapper.toMinestom(posted.block()));
            }
        });

        node.addListener(InventoryPreClickEvent.class, event -> {
            var posted = bus.post(new InventoryClickEvent(
                    players.wrap(event.getPlayer()), new TubeInventory(event.getInventory()),
                    event.getSlot(), ItemMapper.fromMinestom(event.getClickedItem())));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(net.minestom.server.event.inventory.InventoryCloseEvent.class, event ->
                bus.post(new InventoryCloseEvent(players.wrap(event.getPlayer()), new TubeInventory(event.getInventory()))));

        node.addListener(net.minestom.server.event.entity.EntityDamageEvent.class, event -> {
            var posted = bus.post(new EntityDamageEvent((LivingEntity) worlds.wrap(event.getEntity()), event.getDamage().getAmount()));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(net.minestom.server.event.entity.EntityDeathEvent.class, event ->
                bus.post(new EntityDeathEvent((LivingEntity) worlds.wrap(event.getEntity()))));

        node.addListener(net.minestom.server.event.entity.EntitySpawnEvent.class, event -> {
            if (!(event.getEntity() instanceof net.minestom.server.entity.Player)) {
                bus.post(new EntitySpawnEvent(worlds.wrap(event.getEntity())));
            }
        });
    }
}