package space.nows.tube.api.network;

import java.net.InetSocketAddress;

public interface Network {

    TcpServer listenTcp(InetSocketAddress address, TcpHandler handler);

    UdpSocket bindUdp(InetSocketAddress address, UdpHandler handler);

    PluginChannels channels();
}
