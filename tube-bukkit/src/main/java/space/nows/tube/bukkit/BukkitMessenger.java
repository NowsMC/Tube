package space.nows.tube.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;
import space.nows.tube.api.event.Subscription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

final class BukkitMessenger implements Messenger {

    private record Incoming(PluginMessageListenerRegistration registration, Subscription subscription) {
    }

    private final BukkitRuntime runtime;
    private final List<Incoming> incoming = new ArrayList<>();
    private final Set<Outgoing> outgoing = new LinkedHashSet<>();

    private record Outgoing(Plugin plugin, String channel) {
    }

    BukkitMessenger(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    static String wire(String channel) {
        return channel.equals("BungeeCord") ? "bungeecord:main" : channel;
    }

    @Override
    public synchronized boolean isOutgoingChannelRegistered(Plugin plugin, String channel) {
        return outgoing.contains(new Outgoing(plugin, channel));
    }

    @Override
    public synchronized boolean isIncomingChannelRegistered(Plugin plugin, String channel) {
        return incoming.stream().anyMatch(entry -> entry.registration().getPlugin() == plugin && entry.registration().getChannel().equals(channel));
    }

    @Override
    public synchronized void registerOutgoingPluginChannel(Plugin plugin, String channel) {
        outgoing.add(new Outgoing(plugin, channel));
    }

    @Override
    public synchronized void unregisterOutgoingPluginChannel(Plugin plugin, String channel) {
        outgoing.remove(new Outgoing(plugin, channel));
    }

    @Override
    public synchronized void unregisterOutgoingPluginChannel(Plugin plugin) {
        outgoing.removeIf(entry -> entry.plugin() == plugin);
    }

    @Override
    public synchronized PluginMessageListenerRegistration registerIncomingPluginChannel(Plugin plugin, String channel, PluginMessageListener listener) {
        if (incoming.stream().anyMatch(entry -> entry.registration().getPlugin() == plugin && entry.registration().getChannel().equals(channel) && entry.registration().getListener() == listener)) {
            throw new IllegalArgumentException("This registration already exists");
        }
        PluginMessageListenerRegistration registration = new PluginMessageListenerRegistration(this, plugin, channel, listener);
        Subscription subscription = runtime.tube.network().channels().register(wire(channel), (player, ignored, data) -> deliver(registration, runtime.wrappers.player(player), data));
        incoming.add(new Incoming(registration, subscription));
        return registration;
    }

    private void deliver(PluginMessageListenerRegistration registration, Player player, byte[] data) {
        if (!registration.getPlugin().isEnabled()) {
            return;
        }
        try {
            registration.getListener().onPluginMessageReceived(registration.getChannel(), player, data);
        } catch (Throwable e) {
            runtime.julLogger.log(Level.SEVERE, "Could not pass plugin message on " + registration.getChannel() + " to " + registration.getPlugin().getName(), e);
        }
    }

    private void remove(java.util.function.Predicate<PluginMessageListenerRegistration> filter) {
        incoming.removeIf(entry -> {
            boolean match = filter.test(entry.registration());
            if (match) {
                entry.subscription().cancel();
            }
            return match;
        });
    }

    @Override
    public synchronized void unregisterIncomingPluginChannel(Plugin plugin, String channel, PluginMessageListener listener) {
        remove(registration -> registration.getPlugin() == plugin && registration.getChannel().equals(channel) && registration.getListener() == listener);
    }

    @Override
    public synchronized void unregisterIncomingPluginChannel(Plugin plugin, String channel) {
        remove(registration -> registration.getPlugin() == plugin && registration.getChannel().equals(channel));
    }

    @Override
    public synchronized void unregisterIncomingPluginChannel(Plugin plugin) {
        remove(registration -> registration.getPlugin() == plugin);
    }

    @Override
    public synchronized Set<String> getOutgoingChannels() {
        Set<String> result = new HashSet<>();
        outgoing.forEach(entry -> result.add(entry.channel()));
        return result;
    }

    @Override
    public synchronized Set<String> getOutgoingChannels(Plugin plugin) {
        Set<String> result = new HashSet<>();
        outgoing.stream().filter(entry -> entry.plugin() == plugin).forEach(entry -> result.add(entry.channel()));
        return result;
    }

    @Override
    public synchronized Set<String> getIncomingChannels() {
        Set<String> result = new HashSet<>();
        incoming.forEach(entry -> result.add(entry.registration().getChannel()));
        return result;
    }

    @Override
    public synchronized Set<String> getIncomingChannels(Plugin plugin) {
        Set<String> result = new HashSet<>();
        incoming.stream().filter(entry -> entry.registration().getPlugin() == plugin).forEach(entry -> result.add(entry.registration().getChannel()));
        return result;
    }

    @Override
    public void dispatchIncomingMessage(Player source, String channel, byte[] message) {
        List<PluginMessageListenerRegistration> matches;
        synchronized (this) {
            matches = incoming.stream().map(Incoming::registration).filter(registration -> registration.getChannel().equals(channel)).toList();
        }
        matches.forEach(registration -> deliver(registration, source, message));
    }

    void send(Plugin plugin, space.nows.tube.api.Player player, String channel, byte[] message) {
        if (!isOutgoingChannelRegistered(plugin, channel)) {
            throw new IllegalArgumentException("Plugin " + plugin.getName() + " is not registered for the outgoing channel " + channel);
        }
        runtime.tube.network().channels().send(player, wire(channel), message);
    }

    synchronized Set<String> listening() {
        return getIncomingChannels();
    }

    synchronized void unregisterAll(Plugin plugin) {
        unregisterIncomingPluginChannel(plugin);
        unregisterOutgoingPluginChannel(plugin);
    }
}
