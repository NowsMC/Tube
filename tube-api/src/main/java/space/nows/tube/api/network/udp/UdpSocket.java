package space.nows.tube.api.network.udp;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public interface UdpSocket extends AutoCloseable {

    InetSocketAddress address();

    CompletableFuture<Void> send(InetSocketAddress target, byte[] data);

    @Override
    void close();
}
