package space.nows.tube.server.event;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.click.Click;
import space.nows.tube.api.entity.GameMode;
import space.nows.tube.api.entity.LivingEntity;
import space.nows.tube.api.event.BlockBreakEvent;
import space.nows.tube.api.event.BlockPlaceEvent;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.event.CommandDispatchEvent;
import space.nows.tube.api.event.EntityDamageEvent;
import space.nows.tube.api.event.EntityDeathEvent;
import space.nows.tube.api.event.EntitySpawnEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.InventoryClickEvent;
import space.nows.tube.api.event.InventoryCloseEvent;
import space.nows.tube.api.event.InventoryDragEvent;
import space.nows.tube.api.event.InventoryOpenEvent;
import space.nows.tube.api.event.PlayerGameModeChangeEvent;
import space.nows.tube.api.event.PlayerInteractEvent;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerMoveEvent;
import space.nows.tube.api.event.PlayerQuitEvent;
import space.nows.tube.api.inventory.ClickType;
import space.nows.tube.server.inventory.ItemMapper;
import space.nows.tube.server.inventory.MinestomInventory;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.BlockMapper;
import space.nows.tube.server.world.WorldService;

public final class EventBridge {

    private final EventBus bus;
    private final PlayerService players;
    private final WorldService worlds;

    public EventBridge(EventBus bus, PlayerService players, WorldService worlds) {
        this.bus = bus;
        this.players = players;
        this.worlds = worlds;
    }

    static ClickType clickType(Click click) {
        return switch (click) {
            case Click.Left ignored -> ClickType.LEFT;
            case Click.Right ignored -> ClickType.RIGHT;
            case Click.LeftShift ignored -> ClickType.SHIFT_LEFT;
            case Click.RightShift ignored -> ClickType.SHIFT_RIGHT;
            case Click.Middle ignored -> ClickType.MIDDLE;
            case Click.Double ignored -> ClickType.DOUBLE_CLICK;
            case Click.HotbarSwap ignored -> ClickType.NUMBER_KEY;
            case Click.OffhandSwap ignored -> ClickType.SWAP_OFFHAND;
            case Click.DropSlot drop -> drop.all() ? ClickType.CONTROL_DROP : ClickType.DROP;
            case Click.LeftDropCursor ignored -> ClickType.WINDOW_BORDER_LEFT;
            case Click.RightDropCursor ignored -> ClickType.WINDOW_BORDER_RIGHT;
            default -> ClickType.UNKNOWN;
        };
    }

    private static void announce(Component message) {
        if (message != null) {
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(message));
        }
    }

    public void install(EventNode<net.minestom.server.event.Event> node) {
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
            var to = new space.nows.tube.api.world.Location(player.world(), target.x(), target.y(), target.z(), target.yaw(), target.pitch());
            var posted = bus.post(new PlayerMoveEvent(player, player.location(), to));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            } else if (!posted.to().equals(to)) {
                var changed = posted.to();
                event.setNewPosition(new net.minestom.server.coordinate.Pos(changed.x(), changed.y(), changed.z(), changed.yaw(), changed.pitch()));
            }
        });

        node.addListener(PlayerCommandEvent.class, event -> {
            var posted = bus.post(new CommandDispatchEvent(players.wrap(event.getPlayer()), event.getCommand()));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            } else if (!posted.line().equals(event.getCommand())) {
                event.setCommand(posted.line());
            }
        });

        node.addListener(net.minestom.server.event.player.PlayerGameModeChangeEvent.class, event -> {
            var posted = bus.post(new PlayerGameModeChangeEvent(players.wrap(event.getPlayer()), GameMode.valueOf(event.getNewGameMode().name())));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(net.minestom.server.event.inventory.InventoryOpenEvent.class, event -> {
            var posted = bus.post(new InventoryOpenEvent(players.wrap(event.getPlayer()), new MinestomInventory(event.getInventory())));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerRespawnEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var target = event.getRespawnPosition();
            var at = new space.nows.tube.api.world.Location(player.world(), target.x(), target.y(), target.z(), target.yaw(), target.pitch());
            var posted = bus.post(new space.nows.tube.api.event.PlayerRespawnEvent(player, at));
            if (!posted.location().equals(at)) {
                var changed = posted.location();
                event.setRespawnPosition(new net.minestom.server.coordinate.Pos(changed.x(), changed.y(), changed.z(), changed.yaw(), changed.pitch()));
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
            var posted = bus.post(new PlayerInteractEvent(player, player.world(), 0, 0, 0, space.nows.tube.api.world.Block.AIR,
                    event.getHand() == net.minestom.server.entity.PlayerHand.MAIN, space.nows.tube.api.event.InteractAction.RIGHT_CLICK_AIR));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerHandAnimationEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var posted = bus.post(new PlayerInteractEvent(player, player.world(), 0, 0, 0, space.nows.tube.api.world.Block.AIR,
                    event.getHand() == net.minestom.server.entity.PlayerHand.MAIN, space.nows.tube.api.event.InteractAction.LEFT_CLICK_AIR));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(InventoryPreClickEvent.class, event -> {
            Click click = event.getClick();
            var player = players.wrap(event.getPlayer());
            var inventory = new MinestomInventory(event.getInventory());
            Cancellable posted = switch (click) {
                case Click.LeftDrag drag -> bus.post(new InventoryDragEvent(player, inventory, drag.slots(), false));
                case Click.RightDrag drag -> bus.post(new InventoryDragEvent(player, inventory, drag.slots(), true));
                case Click.MiddleDrag drag -> bus.post(new InventoryDragEvent(player, inventory, drag.slots(), false));
                default -> bus.post(new InventoryClickEvent(player, inventory, event.getSlot(), ItemMapper.fromMinestom(event.getClickedItem()),
                        clickType(click), click instanceof Click.HotbarSwap swap ? swap.hotbarSlot() : -1));
            };
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(net.minestom.server.event.inventory.InventoryCloseEvent.class, event ->
                bus.post(new InventoryCloseEvent(players.wrap(event.getPlayer()), new MinestomInventory(event.getInventory()))));

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