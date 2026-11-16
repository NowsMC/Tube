package space.nows.tube.api.network;

import java.net.InetSocketAddress;

public interface TcpServer extends AutoCloseable {

    InetSocketAddress address();

    @Override
    void close();
}
