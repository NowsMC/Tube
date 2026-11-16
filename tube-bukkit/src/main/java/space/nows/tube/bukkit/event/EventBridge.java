package space.nows.tube.bukkit.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import space.nows.tube.api.event.BlockBreakEvent;
import space.nows.tube.api.event.BlockPlaceEvent;
import space.nows.tube.api.event.EntityDamageEvent;
import space.nows.tube.api.event.EntityDeathEvent;
import space.nows.tube.api.event.EntityPickupItemEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.InventoryClickEvent;
import space.nows.tube.api.event.InventoryCloseEvent;
import space.nows.tube.api.event.PlayerChatEvent;
import space.nows.tube.api.event.PlayerDropItemEvent;
import space.nows.tube.api.event.PlayerInteractEvent;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerMoveEvent;
import space.nows.tube.api.event.PlayerPreLoginEvent;
import space.nows.tube.api.event.PlayerQuitEvent;
import space.nows.tube.api.event.PlayerRespawnEvent;
import space.nows.tube.api.event.PlayerTeleportEvent;
import space.nows.tube.api.event.Subscription;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.inventory.BukkitInventory;
import space.nows.tube.bukkit.inventory.ItemConverter;

import java.util.HashSet;
import java.util.List;

public final class EventBridge {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final BukkitRuntime runtime;

    public EventBridge(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    public void install(EventBus bus, List<Subscription> subscriptions) {
        subscriptions.add(bus.subscribe(PlayerJoinEvent.class, EventPriority.NORMAL, false, this::join));
        subscriptions.add(bus.subscribe(PlayerQuitEvent.class, EventPriority.NORMAL, false, this::quit));
        subscriptions.add(bus.subscribe(PlayerChatEvent.class, EventPriority.NORMAL, false, this::chat));
        subscriptions.add(bus.subscribe(BlockBreakEvent.class, EventPriority.NORMAL, false, this::blockBreak));
        subscriptions.add(bus.subscribe(BlockPlaceEvent.class, EventPriority.NORMAL, false, this::blockPlace));
        subscriptions.add(bus.subscribe(InventoryClickEvent.class, EventPriority.NORMAL, false, this::inventoryClick));
        subscriptions.add(bus.subscribe(InventoryCloseEvent.class, EventPriority.NORMAL, false, this::inventoryClose));
        subscriptions.add(bus.subscribe(EntityDamageEvent.class, EventPriority.NORMAL, false, this::entityDamage));
        subscriptions.add(bus.subscribe(EntityDeathEvent.class, EventPriority.NORMAL, false, this::entityDeath));
        subscriptions.add(bus.subscribe(PlayerTeleportEvent.class, EventPriority.NORMAL, false, this::teleport));
        subscriptions.add(bus.subscribe(PlayerMoveEvent.class, EventPriority.NORMAL, false, this::move));
        subscriptions.add(bus.subscribe(PlayerInteractEvent.class, EventPriority.NORMAL, false, this::interact));
        subscriptions.add(bus.subscribe(PlayerDropItemEvent.class, EventPriority.NORMAL, false, this::dropItem));
        subscriptions.add(bus.subscribe(EntityPickupItemEvent.class, EventPriority.NORMAL, false, this::pickupItem));
        subscriptions.add(bus.subscribe(PlayerRespawnEvent.class, EventPriority.NORMAL, false, this::respawn));
        subscriptions.add(bus.subscribe(PlayerPreLoginEvent.class, EventPriority.NORMAL, false, this::preLogin));
    }

    private void join(PlayerJoinEvent event) {
        var player = runtime.adapters().player(event.player());
        var bukkit = new org.bukkit.event.player.PlayerJoinEvent(player, text(event.joinMessage()));
        runtime.pluginManager().callEvent(bukkit);
        event.setJoinMessage(component(bukkit.getJoinMessage()));
    }

    private void quit(PlayerQuitEvent event) {
        var player = runtime.adapters().player(event.player());
        var bukkit = new org.bukkit.event.player.PlayerQuitEvent(player, text(event.quitMessage()));
        runtime.pluginManager().callEvent(bukkit);
        event.setQuitMessage(component(bukkit.getQuitMessage()));
        runtime.adapters().forget(event.player().uuid());
    }

    private void chat(PlayerChatEvent event) {
        var player = runtime.adapters().player(event.player());
        var recipients = new HashSet<>(runtime.adapters().onlinePlayers());
        var bukkit = new org.bukkit.event.player.AsyncPlayerChatEvent(false, player, event.message(), recipients);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        event.setMessage(bukkit.getMessage());
    }

    private void blockBreak(BlockBreakEvent event) {
        var block = runtime.adapters().world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var bukkit = new org.bukkit.event.block.BlockBreakEvent(block, runtime.adapters().player(event.player()));
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void blockPlace(BlockPlaceEvent event) {
        var player = runtime.adapters().player(event.player());
        var block = runtime.adapters().world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var bukkit = new org.bukkit.event.block.BlockPlaceEvent(block, player.getInventory().getItemInMainHand(), player);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled() || !bukkit.canBuild());
    }

    private void inventoryClick(InventoryClickEvent event) {
        var player = runtime.adapters().player(event.player());
        var bukkit = new org.bukkit.event.inventory.InventoryClickEvent(player, inventory(event.player(), event.inventory()), event.slot(), ItemConverter.fromTube(event.clicked()));
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void inventoryClose(InventoryCloseEvent event) {
        var player = runtime.adapters().player(event.player());
        runtime.pluginManager().callEvent(new org.bukkit.event.inventory.InventoryCloseEvent(player, inventory(event.player(), event.inventory())));
    }

    private void entityDamage(EntityDamageEvent event) {
        var bukkit = new org.bukkit.event.entity.EntityDamageEvent(runtime.adapters().living(event.entity()), DamageCauses.fromKey(event.cause()), event.damage());
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void entityDeath(EntityDeathEvent event) {
        if (event.entity() instanceof space.nows.tube.api.entity.Player tubePlayer) {
            var player = runtime.adapters().player(tubePlayer);
            runtime.pluginManager().callEvent(new org.bukkit.event.entity.PlayerDeathEvent(player, player.getName() + " died"));
        } else {
            runtime.pluginManager().callEvent(new org.bukkit.event.entity.EntityDeathEvent(runtime.adapters().living(event.entity())));
        }
    }

    private void move(PlayerMoveEvent event) {
        var player = runtime.adapters().player(event.player());
        var to = runtime.adapters().location(event.to());
        var bukkit = new org.bukkit.event.player.PlayerMoveEvent(player, runtime.adapters().location(event.from()), to);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
        } else if (bukkit.getTo() != to) {
            event.setTo(runtime.adapters().tubeLocation(bukkit.getTo()));
        }
    }

    private void teleport(PlayerTeleportEvent event) {
        var player = runtime.adapters().player(event.player());
        var to = runtime.adapters().location(event.to());
        var bukkit = new org.bukkit.event.player.PlayerTeleportEvent(player, runtime.adapters().location(event.from()), to, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
        } else if (bukkit.getTo() != to) {
            event.setTo(runtime.adapters().tubeLocation(bukkit.getTo()));
        }
    }

    private void interact(PlayerInteractEvent event) {
        var player = runtime.adapters().player(event.player());
        var block = runtime.adapters().world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var hand = event.mainHand() ? org.bukkit.inventory.EquipmentSlot.HAND : org.bukkit.inventory.EquipmentSlot.OFF_HAND;
        var item = event.mainHand() ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        var action = switch (event.action()) {
            case LEFT_CLICK_BLOCK -> org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
            case LEFT_CLICK_AIR -> org.bukkit.event.block.Action.LEFT_CLICK_AIR;
            case RIGHT_CLICK_BLOCK -> org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
            case RIGHT_CLICK_AIR -> org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
        };
        boolean air = action == org.bukkit.event.block.Action.LEFT_CLICK_AIR || action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
        var bukkit = new org.bukkit.event.player.PlayerInteractEvent(player, action, item, air ? null : block, air ? null : org.bukkit.block.BlockFace.SELF, hand);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void dropItem(PlayerDropItemEvent event) {
        var bukkit = new org.bukkit.event.player.PlayerDropItemEvent(runtime.adapters().player(event.player()), runtime.adapters().item(event.item()));
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void pickupItem(EntityPickupItemEvent event) {
        var item = runtime.adapters().item(event.item());
        var entity = runtime.adapters().living(event.entity());
        boolean cancelled = false;
        if (entity instanceof org.bukkit.entity.Player player) {
            var legacy = new org.bukkit.event.player.PlayerPickupItemEvent(player, item, 0);
            runtime.pluginManager().callEvent(legacy);
            cancelled = legacy.isCancelled();
        }
        var bukkit = new org.bukkit.event.entity.EntityPickupItemEvent(entity, item, 0);
        bukkit.setCancelled(cancelled);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void preLogin(PlayerPreLoginEvent event) {
        var address = event.address() instanceof java.net.InetSocketAddress inet ? inet.getAddress() : null;
        var bukkit = new org.bukkit.event.player.AsyncPlayerPreLoginEvent(event.name(), address, event.uuid());
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            String message = bukkit.getKickMessage();
            event.deny(message == null || message.isEmpty() ? Component.text("You are not allowed to join") : LEGACY.deserialize(message));
        }
    }

    private void respawn(PlayerRespawnEvent event) {
        var location = runtime.adapters().location(event.location());
        var bukkit = new org.bukkit.event.player.PlayerRespawnEvent(runtime.adapters().player(event.player()), location, false);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.getRespawnLocation() != location) {
            event.setLocation(runtime.adapters().tubeLocation(bukkit.getRespawnLocation()));
        }
    }

    private org.bukkit.inventory.Inventory inventory(space.nows.tube.api.entity.Player owner, space.nows.tube.api.inventory.Inventory inventory) {
        if (inventory.equals(owner.inventory())) {
            return (org.bukkit.inventory.Inventory) runtime.adapters().player(owner).getInventory();
        }
        return new BukkitInventory(inventory);
    }

    private static String text(Component component) {
        return component == null ? null : LEGACY.serialize(component);
    }

    private static Component component(String text) {
        return text == null || text.isEmpty() ? null : LEGACY.deserialize(text);
    }
}
