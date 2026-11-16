package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
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
import space.nows.tube.api.event.PlayerInteractEvent;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerMoveEvent;
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

        node.addListener(net.minestom.server.event.player.PlayerMoveEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var target = event.getNewPosition();
            var to = new space.nows.tube.api.Location(player.world(), target.x(), target.y(), target.z(), target.yaw(), target.pitch());
            var posted = bus.post(new PlayerMoveEvent(player, player.location(), to));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            } else if (!posted.to().equals(to)) {
                var changed = posted.to();
                event.setNewPosition(new net.minestom.server.coordinate.Pos(changed.x(), changed.y(), changed.z(), changed.yaw(), changed.pitch()));
            }
        });

        node.addListener(PlayerBlockInteractEvent.class, event -> {
            var pos = event.getBlockPosition();
            var posted = bus.post(new PlayerInteractEvent(
                    players.wrap(event.getPlayer()), worlds.of(event.getInstance()),
                    pos.blockX(), pos.blockY(), pos.blockZ(), BlockMapper.fromMinestom(event.getBlock()),
                    event.getHand() == net.minestom.server.entity.PlayerHand.MAIN));
            if (posted.isCancelled()) {
                event.setCancelled(true);
                event.setBlockingItemUse(true);
            }
        });

        node.addListener(PlayerStartDiggingEvent.class, event -> {
            var pos = event.getBlockPosition();
            var posted = bus.post(new PlayerInteractEvent(
                    players.wrap(event.getPlayer()), worlds.of(event.getInstance()),
                    pos.blockX(), pos.blockY(), pos.blockZ(), BlockMapper.fromMinestom(event.getBlock()), true,
                    space.nows.tube.api.event.InteractAction.LEFT_CLICK_BLOCK));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerUseItemEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var posted = bus.post(new PlayerInteractEvent(player, player.world(), 0, 0, 0, space.nows.tube.api.Block.AIR,
                    event.getHand() == net.minestom.server.entity.PlayerHand.MAIN, space.nows.tube.api.event.InteractAction.RIGHT_CLICK_AIR));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerHandAnimationEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var posted = bus.post(new PlayerInteractEvent(player, player.world(), 0, 0, 0, space.nows.tube.api.Block.AIR,
                    event.getHand() == net.minestom.server.entity.PlayerHand.MAIN, space.nows.tube.api.event.InteractAction.LEFT_CLICK_AIR));
            if (posted.isCancelled()) {
                event.setCancelled(true);
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
            var posted = bus.post(new EntityDamageEvent((LivingEntity) worlds.wrap(event.getEntity()), event.getDamage().getAmount(), event.getDamage().getType().key().asString()));
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