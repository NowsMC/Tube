package space.nows.tube.api.network;

import space.nows.tube.api.entity.Player;
import space.nows.tube.api.event.Subscription;

public interface PluginChannels {

    Subscription register(String channel, ChannelHandler handler);

    void send(Player player, String channel, byte[] data);

    @FunctionalInterface
    interface ChannelHandler {

        void received(Player player, String channel, byte[] data);
    }
}
