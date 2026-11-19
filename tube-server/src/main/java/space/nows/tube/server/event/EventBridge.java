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
import net.minestom.server.event.player.PlayerInputEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerStartFlyingWithElytraEvent;
import net.minestom.server.event.player.PlayerStartSprintingEvent;
import net.minestom.server.event.player.PlayerStopFlyingWithElytraEvent;
import net.minestom.server.event.player.PlayerStopSprintingEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.click.Click;
import space.nows.tube.api.entity.LivingEntity;
import space.nows.tube.api.entity.player.GameMode;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.block.BlockBreakEvent;
import space.nows.tube.api.event.block.BlockPlaceEvent;
import space.nows.tube.api.event.entity.EntityDamageEvent;
import space.nows.tube.api.event.entity.EntityDeathEvent;
import space.nows.tube.api.event.entity.EntityGlideEvent;
import space.nows.tube.api.event.entity.EntitySpawnEvent;
import space.nows.tube.api.event.inventory.InventoryClickEvent;
import space.nows.tube.api.event.inventory.InventoryCloseEvent;
import space.nows.tube.api.event.inventory.InventoryDragEvent;
import space.nows.tube.api.event.inventory.InventoryOpenEvent;
import space.nows.tube.api.event.player.PlayerGameModeChangeEvent;
import space.nows.tube.api.event.player.PlayerInteractEvent;
import space.nows.tube.api.event.player.PlayerJoinEvent;
import space.nows.tube.api.event.player.PlayerMoveEvent;
import space.nows.tube.api.event.player.PlayerQuitEvent;
import space.nows.tube.api.event.player.PlayerSneakEvent;
import space.nows.tube.api.event.player.PlayerSprintEvent;
import space.nows.tube.api.event.server.CommandDispatchEvent;
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
    private volatile DeathDrops deathDrops = DeathDrops.NONE;

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

        node.addListener(PlayerInputEvent.class, event -> {
            if (event.hasPressedShiftKey() || event.hasReleasedShiftKey()) {
                bus.post(new PlayerSneakEvent(players.wrap(event.getPlayer()), event.isHoldingShiftKey()));
            }
        });
        node.addListener(PlayerStartSprintingEvent.class, event -> bus.post(new PlayerSprintEvent(players.wrap(event.getPlayer()), true)));
        node.addListener(PlayerStopSprintingEvent.class, event -> bus.post(new PlayerSprintEvent(players.wrap(event.getPlayer()), false)));
        node.addListener(PlayerStartFlyingWithElytraEvent.class, event -> bus.post(new EntityGlideEvent(players.wrap(event.getPlayer()), true)));
        node.addListener(PlayerStopFlyingWithElytraEvent.class, event -> bus.post(new EntityGlideEvent(players.wrap(event.getPlayer()), false)));

        node.addListener(PlayerChatEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var posted = bus.post(new space.nows.tube.api.event.player.PlayerChatEvent(player, event.getRawMessage()));
            if (posted.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            event.setFormattedMessage(posted.formatted().orElseGet(() -> ChatFormat.of(player, posted.message())));
        });

        node.addListener(PlayerBlockBreakEvent.class, event -> {
            var pos = event.getBlockPosition();
            var posted = bus.post(new BlockBreakEvent(
                    players.wrap(event.getPlayer()), worlds.of(event.getInstance()),
                    pos.blockX(), pos.blockY(), pos.blockZ(), BlockMapper.fromMinestom(event.getBlock())));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            } else if (!posted.dropItems()) {
                event.getPlayer().setTag(BlockBreakDrops.SUPPRESSED_AT, pos);
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
            var posted = bus.post(new space.nows.tube.api.event.player.PlayerRespawnEvent(player, at));
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
                    space.nows.tube.api.event.player.InteractAction.LEFT_CLICK_BLOCK));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerUseItemEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var posted = bus.post(new PlayerInteractEvent(player, player.world(), 0, 0, 0, space.nows.tube.api.world.Block.AIR,
                    event.getHand() == net.minestom.server.entity.PlayerHand.MAIN, space.nows.tube.api.event.player.InteractAction.RIGHT_CLICK_AIR));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerHandAnimationEvent.class, event -> {
            var player = players.wrap(event.getPlayer());
            var posted = bus.post(new PlayerInteractEvent(player, player.world(), 0, 0, 0, space.nows.tube.api.world.Block.AIR,
                    event.getHand() == net.minestom.server.entity.PlayerHand.MAIN, space.nows.tube.api.event.player.InteractAction.LEFT_CLICK_AIR));
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
            var damage = event.getDamage();
            var attacker = damage.getAttacker() == null ? null : worlds.wrap(damage.getAttacker());
            var posted = bus.post(new EntityDamageEvent((LivingEntity) worlds.wrap(event.getEntity()), damage.getAmount(),
                    damage.getType().key().asString(), attacker));
            if (posted.isCancelled()) {
                event.setCancelled(true);
            } else if (posted.damage() != damage.getAmount()) {
                damage.setAmount((float) posted.damage());
            }
        });

        node.addListener(net.minestom.server.event.entity.EntityDeathEvent.class, event -> {
            if (event.getEntity() instanceof net.minestom.server.entity.LivingEntity living) {
                death(living);
            }
        });

        node.addListener(net.minestom.server.event.entity.EntitySpawnEvent.class, event -> {
            if (!(event.getEntity() instanceof net.minestom.server.entity.Player)) {
                var posted = bus.post(new EntitySpawnEvent(worlds.wrap(event.getEntity())));
                if (posted.isCancelled()) {
                    event.getEntity().scheduleNextTick(net.minestom.server.entity.Entity::remove);
                }
            }
        });
    }

    public void setDeathDrops(DeathDrops deathDrops) {
        this.deathDrops = deathDrops;
    }

    private void death(net.minestom.server.entity.LivingEntity entity) {
        DeathDrops.Drops drops = deathDrops.collect(entity);
        java.util.List<space.nows.tube.api.inventory.ItemStack> mapped = new java.util.ArrayList<>();
        for (var item : drops.items()) {
            mapped.add(space.nows.tube.server.inventory.ItemMapper.fromMinestom(item));
        }
        var posted = bus.post(new EntityDeathEvent((LivingEntity) worlds.wrap(entity), mapped, drops.experience()));
        var instance = entity.getInstance();
        if (instance == null) {
            return;
        }
        boolean[] used = new boolean[mapped.size()];
        var position = entity.getPosition().add(0, 0.5, 0);
        for (var item : posted.drops()) {
            net.minestom.server.item.ItemStack stack = null;
            for (int i = 0; i < mapped.size(); i++) {
                if (!used[i] && mapped.get(i).equals(item)) {
                    used[i] = true;
                    stack = drops.items().get(i);
                    break;
                }
            }
            if (stack == null) {
                stack = space.nows.tube.server.inventory.ItemMapper.toMinestom(item);
            }
            if (!stack.isAir()) {
                var dropped = new net.minestom.server.entity.ItemEntity(stack);
                dropped.setPickupDelay(java.time.Duration.ofMillis(500));
                var random = java.util.concurrent.ThreadLocalRandom.current();
                dropped.setVelocity(new net.minestom.server.coordinate.Vec(random.nextDouble(-1, 1), 3, random.nextDouble(-1, 1)));
                dropped.setInstance(instance, position);
            }
        }
        int experience = posted.experience();
        while (experience > 0) {
            int orb = Math.min(experience, 37);
            experience -= orb;
            new net.minestom.server.entity.ExperienceOrb((short) orb).setInstance(instance, position);
        }
    }
}
