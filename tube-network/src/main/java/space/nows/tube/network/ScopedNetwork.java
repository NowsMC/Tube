package space.nows.tube.network;

import space.nows.tube.api.entity.Player;
import space.nows.tube.api.event.Subscription;
import space.nows.tube.api.network.Network;
import space.nows.tube.api.network.PluginChannels;
import space.nows.tube.api.network.TcpHandler;
import space.nows.tube.api.network.TcpServer;
import space.nows.tube.api.network.UdpHandler;
import space.nows.tube.api.network.UdpSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public final class ScopedNetwork implements Network {

    private final Network delegate;
    private final List<AutoCloseable> sockets = new ArrayList<>();
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final PluginChannels channels = new PluginChannels() {
        @Override
        public Subscription register(String channel, ChannelHandler handler) {
            Subscription subscription = delegate.channels().register(channel, handler);
            synchronized (ScopedNetwork.this) {
                subscriptions.add(subscription);
            }
            return subscription;
        }

        @Override
        public void send(Player player, String channel, byte[] data) {
            delegate.channels().send(player, channel, data);
        }
    };

    public ScopedNetwork(Network delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized TcpServer listenTcp(InetSocketAddress address, TcpHandler handler) {
        TcpServer server = delegate.listenTcp(address, handler);
        sockets.add(server);
        return server;
    }

    @Override
    public synchronized UdpSocket bindUdp(InetSocketAddress address, UdpHandler handler) {
        UdpSocket socket = delegate.bindUdp(address, handler);
        sockets.add(socket);
        return socket;
    }

    @Override
    public PluginChannels channels() {
        return channels;
    }

    public synchronized void closeAll() {
        subscriptions.forEach(Subscription::cancel);
        subscriptions.clear();
        for (AutoCloseable socket : sockets) {
            try {
                socket.close();
            } catch (Exception ignored) {
                // already closed or closing
            }
        }
        sockets.clear();
    }
}
