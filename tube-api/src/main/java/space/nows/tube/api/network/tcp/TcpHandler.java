package space.nows.tube.api.network.tcp;

public interface TcpHandler {

    default void connected(TcpConnection connection) {
    }

    void received(TcpConnection connection, byte[] data);

    default void writable(TcpConnection connection) {
    }

    default void closed(TcpConnection connection) {
    }
}
