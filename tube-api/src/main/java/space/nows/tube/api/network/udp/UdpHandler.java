package space.nows.tube.api.network.udp;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface UdpHandler {

    void received(UdpSocket socket, InetSocketAddress sender, byte[] data);
}
