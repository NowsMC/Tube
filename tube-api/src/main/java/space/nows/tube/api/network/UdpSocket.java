package space.nows.tube.api.network;

import java.net.InetSocketAddress;

public interface UdpSocket extends AutoCloseable {

    InetSocketAddress address();

    void send(InetSocketAddress target, byte[] data);

    @Override
    void close();
}
