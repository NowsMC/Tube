package org.bukkit.plugin.messaging;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public interface Messenger {

    boolean isOutgoingChannelRegistered(Plugin plugin, String channel);

    boolean isIncomingChannelRegistered(Plugin plugin, String channel);

    void registerOutgoingPluginChannel(Plugin plugin, String channel);

    void unregisterOutgoingPluginChannel(Plugin plugin, String channel);

    void unregisterOutgoingPluginChannel(Plugin plugin);

    PluginMessageListenerRegistration registerIncomingPluginChannel(Plugin plugin, String channel, PluginMessageListener listener);

    void unregisterIncomingPluginChannel(Plugin plugin, String channel, PluginMessageListener listener);

    void unregisterIncomingPluginChannel(Plugin plugin, String channel);

    void unregisterIncomingPluginChannel(Plugin plugin);

    Set<String> getOutgoingChannels();

    Set<String> getOutgoingChannels(Plugin plugin);

    Set<String> getIncomingChannels();

    Set<String> getIncomingChannels(Plugin plugin);

    void dispatchIncomingMessage(Player source, String channel, byte[] message);
}
