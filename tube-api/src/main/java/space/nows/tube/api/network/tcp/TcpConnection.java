package space.nows.tube.api.network.tcp;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public interface TcpConnection {

    long id();

    InetSocketAddress remoteAddress();

    boolean isOpen();

    boolean isWritable();

    CompletableFuture<Void> send(byte[] data);

    void close();
}
