package space.nows.tube.api.network;

import space.nows.tube.api.network.tcp.TcpHandler;
import space.nows.tube.api.network.tcp.TcpServer;
import space.nows.tube.api.network.udp.UdpHandler;
import space.nows.tube.api.network.udp.UdpSocket;

import java.net.InetSocketAddress;

public interface Network {

    TcpServer listenTcp(InetSocketAddress address, TcpHandler handler);

    UdpSocket bindUdp(InetSocketAddress address, UdpHandler handler);

    PluginChannels channels();
}
