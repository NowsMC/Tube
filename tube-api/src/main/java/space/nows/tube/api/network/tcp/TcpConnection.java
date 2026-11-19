package space.nows.tube.api.network.tcp;

import java.net.InetSocketAddress;

public interface TcpConnection {

    long id();

    InetSocketAddress remoteAddress();

    boolean isOpen();

    void send(byte[] data);

    void close();
}
