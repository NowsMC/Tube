package space.nows.tube.network;

import io.netty.channel.IoHandlerFactory;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public enum Transport {
    NIO,
    EPOLL;

    public static Transport choose(boolean preferNative) {
        return preferNative && Epoll.isAvailable() ? EPOLL : NIO;
    }

    IoHandlerFactory ioHandlerFactory() {
        return this == EPOLL ? EpollIoHandler.newFactory() : NioIoHandler.newFactory();
    }

    Class<? extends ServerSocketChannel> serverChannel() {
        return this == EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    Class<? extends DatagramChannel> datagramChannel() {
        return this == EPOLL ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }
}
