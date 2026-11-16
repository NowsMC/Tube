package space.nows.tube.api.network;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface UdpHandler {

    void received(UdpSocket socket, InetSocketAddress sender, byte[] data);
}
