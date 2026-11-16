package space.nows.tube.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
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

    private static String text(Component component) {
        return component == null ? null : LEGACY.serialize(component);
    }

    private static Component component(String text) {
        return text == null || text.isEmpty() ? null : LEGACY.deserialize(text);
    }
}
