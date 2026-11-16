package org.bukkit.plugin.messaging;

import org.bukkit.plugin.Plugin;

public final class PluginMessageListenerRegistration {

    private final Messenger messenger;
    private final Plugin plugin;
    private final String channel;
    private final PluginMessageListener listener;

    public PluginMessageListenerRegistration(Messenger messenger, Plugin plugin, String channel, PluginMessageListener listener) {
        this.messenger = messenger;
        this.plugin = plugin;
        this.channel = channel;
        this.listener = listener;
    }

    public String getChannel() {
        return channel;
    }

    public PluginMessageListener getListener() {
        return listener;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public boolean isValid() {
        return messenger.isIncomingChannelRegistered(plugin, channel);
    }
}
