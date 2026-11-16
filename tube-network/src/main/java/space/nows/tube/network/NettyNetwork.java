package space.nows.tube.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.network.Network;
import space.nows.tube.api.network.PluginChannels;
import space.nows.tube.api.network.TcpConnection;
import space.nows.tube.api.network.TcpHandler;
import space.nows.tube.api.network.TcpServer;
import space.nows.tube.api.network.UdpHandler;
import space.nows.tube.api.network.UdpSocket;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public final class NettyNetwork implements Network, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-network");

    private final Transport transport;
    private final EventLoopGroup group;
    private final PluginChannels channels;
    private final AtomicLong connectionIds = new AtomicLong();

    public NettyNetwork(PluginChannels channels, Transport transport) {
        this.channels = channels;
        this.transport = transport;
        this.group = new MultiThreadIoEventLoopGroup(2, new DefaultThreadFactory("tube-network", true), transport.ioHandlerFactory());
    }

    public Transport transport() {
        return transport;
    }

    EventLoopGroup group() {
        return group;
    }

    @Override
    public TcpServer listenTcp(InetSocketAddress address, TcpHandler handler) {
        Channel channel = new ServerBootstrap()
                .group(group)
                .channel(transport.serverChannel())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel child) {
                        child.pipeline().addLast(new TcpAdapter(handler, connectionIds.incrementAndGet()));
                    }
                })
                .bind(address)
                .syncUninterruptibly()
                .channel();
        return new BoundTcp(channel);
    }

    @Override
    public UdpSocket bindUdp(InetSocketAddress address, UdpHandler handler) {
        BoundUdp[] holder = new BoundUdp[1];
        Channel channel = new Bootstrap()
                .group(group)
                .channel(transport.datagramChannel())
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext context, DatagramPacket packet) {
                        byte[] data = new byte[packet.content().readableBytes()];
                        packet.content().readBytes(data);
                        try {
                            handler.received(holder[0], packet.sender(), data);
                        } catch (RuntimeException e) {
                            LOGGER.warn("A UDP handler failed on {}", context.channel().localAddress(), e);
                        }
                    }
                })
                .bind(address)
                .syncUninterruptibly()
                .channel();
        holder[0] = new BoundUdp(channel);
        return holder[0];
    }

    @Override
    public PluginChannels channels() {
        return channels;
    }

    @Override
    public void close() {
        group.shutdownGracefully(0, 2, java.util.concurrent.TimeUnit.SECONDS).syncUninterruptibly();
    }

    private static final class BoundTcp implements TcpServer {

        private final Channel channel;

        BoundTcp(Channel channel) {
            this.channel = channel;
        }

        @Override
        public InetSocketAddress address() {
            return (InetSocketAddress) channel.localAddress();
        }

        @Override
        public void close() {
            channel.close().syncUninterruptibly();
        }
    }

    private static final class BoundUdp implements UdpSocket {

        private final Channel channel;

        BoundUdp(Channel channel) {
            this.channel = channel;
        }

        @Override
        public InetSocketAddress address() {
            return (InetSocketAddress) channel.localAddress();
        }

        @Override
        public void send(InetSocketAddress target, byte[] data) {
            channel.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(data), target));
        }

        @Override
        public void close() {
            channel.close().syncUninterruptibly();
        }
    }

    private static final class TcpAdapter extends ChannelInboundHandlerAdapter implements TcpConnection {

        private final TcpHandler handler;
        private final long id;
        private volatile Channel channel;

        TcpAdapter(TcpHandler handler, long id) {
            this.handler = handler;
            this.id = id;
        }

        @Override
        public void channelActive(ChannelHandlerContext context) {
            channel = context.channel();
            guard(context, () -> handler.connected(this));
            context.fireChannelActive();
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object message) {
            ByteBuf buffer = (ByteBuf) message;
            try {
                byte[] data = new byte[buffer.readableBytes()];
                buffer.readBytes(data);
                guard(context, () -> handler.received(this, data));
            } finally {
                buffer.release();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext context) {
            guard(context, () -> handler.closed(this));
            context.fireChannelInactive();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            LOGGER.debug("Closing {} after an error", context.channel().remoteAddress(), cause);
            context.close();
        }

        private void guard(ChannelHandlerContext context, Runnable action) {
            try {
                action.run();
            } catch (RuntimeException e) {
                LOGGER.warn("A TCP handler failed for {}", context.channel().remoteAddress(), e);
                context.close();
            }
        }

        @Override
        public long id() {
            return id;
        }

        @Override
        public InetSocketAddress remoteAddress() {
            return (InetSocketAddress) channel.remoteAddress();
        }

        @Override
        public boolean isOpen() {
            return channel != null && channel.isActive();
        }

        @Override
        public void send(byte[] data) {
            ChannelFuture future = channel.writeAndFlush(Unpooled.wrappedBuffer(data));
            future.addListener(done -> {
                if (!done.isSuccess()) {
                    LOGGER.debug("Could not write to {}", channel.remoteAddress(), done.cause());
                }
            });
        }

        @Override
        public void close() {
            channel.close();
        }
    }
}
