package space.nows.tube.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import space.nows.tube.api.event.BlockBreakEvent;
import space.nows.tube.api.event.BlockPlaceEvent;
import space.nows.tube.api.event.EntityDamageEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.InventoryClickEvent;
import space.nows.tube.api.event.InventoryCloseEvent;
import space.nows.tube.api.event.PlayerChatEvent;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerQuitEvent;
import space.nows.tube.api.event.Subscription;

import java.util.HashSet;
import java.util.List;

final class BukkitEventBridge {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final BukkitRuntime runtime;

    BukkitEventBridge(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    void install(EventBus bus, List<Subscription> subscriptions) {
        subscriptions.add(bus.subscribe(PlayerJoinEvent.class, EventPriority.NORMAL, false, this::join));
        subscriptions.add(bus.subscribe(PlayerQuitEvent.class, EventPriority.NORMAL, false, this::quit));
        subscriptions.add(bus.subscribe(PlayerChatEvent.class, EventPriority.NORMAL, false, this::chat));
        subscriptions.add(bus.subscribe(BlockBreakEvent.class, EventPriority.NORMAL, false, this::blockBreak));
        subscriptions.add(bus.subscribe(BlockPlaceEvent.class, EventPriority.NORMAL, false, this::blockPlace));
        subscriptions.add(bus.subscribe(InventoryClickEvent.class, EventPriority.NORMAL, false, this::inventoryClick));
        subscriptions.add(bus.subscribe(InventoryCloseEvent.class, EventPriority.NORMAL, false, this::inventoryClose));
        subscriptions.add(bus.subscribe(EntityDamageEvent.class, EventPriority.NORMAL, false, this::entityDamage));
    }

    private void join(PlayerJoinEvent event) {
        var player = runtime.wrappers.player(event.player());
        var bukkit = new org.bukkit.event.player.PlayerJoinEvent(player, text(event.joinMessage()));
        runtime.pluginManager.callEvent(bukkit);
        event.setJoinMessage(component(bukkit.getJoinMessage()));
    }

    private void quit(PlayerQuitEvent event) {
        var player = runtime.wrappers.player(event.player());
        var bukkit = new org.bukkit.event.player.PlayerQuitEvent(player, text(event.quitMessage()));
        runtime.pluginManager.callEvent(bukkit);
        event.setQuitMessage(component(bukkit.getQuitMessage()));
        runtime.wrappers.forget(event.player().uuid());
    }

    private void chat(PlayerChatEvent event) {
        var player = runtime.wrappers.player(event.player());
        var recipients = new HashSet<>(runtime.wrappers.onlinePlayers());
        var bukkit = new org.bukkit.event.player.AsyncPlayerChatEvent(false, player, event.message(), recipients);
        runtime.pluginManager.callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        event.setMessage(bukkit.getMessage());
    }

    private void blockBreak(BlockBreakEvent event) {
        var block = runtime.wrappers.world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var bukkit = new org.bukkit.event.block.BlockBreakEvent(block, runtime.wrappers.player(event.player()));
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void blockPlace(BlockPlaceEvent event) {
        var player = runtime.wrappers.player(event.player());
        var block = runtime.wrappers.world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var bukkit = new org.bukkit.event.block.BlockPlaceEvent(block, player.getInventory().getItemInMainHand(), player);
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled() || !bukkit.canBuild());
    }

    private void inventoryClick(InventoryClickEvent event) {
        var player = runtime.wrappers.player(event.player());
        var bukkit = new org.bukkit.event.inventory.InventoryClickEvent(player, inventory(event.player(), event.inventory()), event.slot(), Items.fromTube(event.clicked()));
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void inventoryClose(InventoryCloseEvent event) {
        var player = runtime.wrappers.player(event.player());
        runtime.pluginManager.callEvent(new org.bukkit.event.inventory.InventoryCloseEvent(player, inventory(event.player(), event.inventory())));
    }

    private void entityDamage(EntityDamageEvent event) {
        var bukkit = new org.bukkit.event.entity.EntityDamageEvent(runtime.wrappers.living(event.entity()), event.damage());
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private org.bukkit.inventory.Inventory inventory(space.nows.tube.api.Player owner, space.nows.tube.api.Inventory inventory) {
        if (inventory.equals(owner.inventory())) {
            return (org.bukkit.inventory.Inventory) runtime.wrappers.player(owner).getInventory();
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
