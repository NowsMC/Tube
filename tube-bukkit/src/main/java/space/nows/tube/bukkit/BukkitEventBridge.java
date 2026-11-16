package space.nows.tube.bukkit;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.PlayerChatEvent;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerQuitEvent;
import space.nows.tube.api.event.Subscription;

import java.util.HashSet;
import java.util.List;

final class BukkitEventBridge {

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
        var bukkit = new org.bukkit.event.player.PlayerJoinEvent(player, event.joinMessage());
        runtime.pluginManager.callEvent(bukkit);
        event.setJoinMessage(bukkit.joinMessage());
    }

    private void quit(PlayerQuitEvent event) {
        var player = runtime.wrappers.player(event.player());
        var bukkit = new org.bukkit.event.player.PlayerQuitEvent(player, event.quitMessage());
        runtime.pluginManager.callEvent(bukkit);
        event.setQuitMessage(bukkit.quitMessage());
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

    @SuppressWarnings("unused")
    private static String plain(net.kyori.adventure.text.Component component) {
        return component == null ? "" : PlainTextComponentSerializer.plainText().serialize(component);
    }
}
