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
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.network.Network;
import space.nows.tube.api.network.PluginChannels;
import space.nows.tube.api.network.tcp.TcpConnection;
import space.nows.tube.api.network.tcp.TcpHandler;
import space.nows.tube.api.network.tcp.TcpServer;
import space.nows.tube.api.network.udp.UdpHandler;
import space.nows.tube.api.network.udp.UdpSocket;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class NettyNetwork implements Network, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-network");
    private static final int PAUSE_READING_BYTES = 1 << 20;
    private static final int RESUME_READING_BYTES = 256 << 10;
    private static final int MAX_QUEUED_DATAGRAMS = 1024;

    private final Transport transport;
    private final EventLoopGroup group;
    private final PluginChannels channels;
    private final AtomicLong connectionIds = new AtomicLong();
    private final ExecutorService handlers = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("tube-network-handler-", 0).factory());

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
        ChannelGroup connections = new DefaultChannelGroup(group.next());
        Channel channel = Binding.await(new ServerBootstrap()
                .group(group)
                .channel(transport.serverChannel())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel child) {
                        connections.add(child);
                        child.pipeline().addLast(new TcpAdapter(handler, connectionIds.incrementAndGet(), new SerialExecutor(handlers)));
                    }
                })
                .bind(address), address);
        return new BoundTcp(channel, connections);
    }

    @Override
    public UdpSocket bindUdp(InetSocketAddress address, UdpHandler handler) {
        AtomicReference<BoundUdp> holder = new AtomicReference<>();
        SerialExecutor queue = new SerialExecutor(handlers);
        Channel channel = Binding.await(new Bootstrap()
                .group(group)
                .channel(transport.datagramChannel())
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext context, DatagramPacket packet) {
                        if (queue.queued() >= MAX_QUEUED_DATAGRAMS) {
                            LOGGER.debug("Dropping a datagram from {}, the handler on {} is behind", packet.sender(), context.channel().localAddress());
                            return;
                        }
                        byte[] data = new byte[packet.content().readableBytes()];
                        packet.content().readBytes(data);
                        BoundUdp current = holder.get();
                        BoundUdp socket = current != null ? current : new BoundUdp(context.channel());
                        InetSocketAddress sender = packet.sender();
                        queue.execute(() -> {
                            try {
                                handler.received(socket, sender, data);
                            } catch (RuntimeException | LinkageError e) {
                                LOGGER.warn("A UDP handler failed on {}", socket.address(), e);
                            }
                        });
                    }
                })
                .bind(address), address);
        BoundUdp socket = new BoundUdp(channel);
        holder.set(socket);
        return socket;
    }

    @Override
    public PluginChannels channels() {
        return channels;
    }

    @Override
    public void close() {
        group.shutdownGracefully(0, 2, java.util.concurrent.TimeUnit.SECONDS).syncUninterruptibly();
        handlers.shutdown();
    }

    private static final class BoundTcp implements TcpServer {

        private final Channel channel;
        private final ChannelGroup connections;

        BoundTcp(Channel channel, ChannelGroup connections) {
            this.channel = channel;
            this.connections = connections;
        }

        @Override
        public InetSocketAddress address() {
            return (InetSocketAddress) channel.localAddress();
        }

        @Override
        public void close() {
            channel.close().syncUninterruptibly();
            connections.close().awaitUninterruptibly();
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
        private final SerialExecutor queue;
        private final AtomicLong pending = new AtomicLong();
        private volatile Channel channel;

        TcpAdapter(TcpHandler handler, long id, SerialExecutor queue) {
            this.handler = handler;
            this.id = id;
            this.queue = queue;
        }

        @Override
        public void channelActive(ChannelHandlerContext context) {
            channel = context.channel();
            queue.execute(() -> guard(() -> handler.connected(this)));
            context.fireChannelActive();
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object message) {
            ByteBuf buffer = (ByteBuf) message;
            byte[] data;
            try {
                data = new byte[buffer.readableBytes()];
                buffer.readBytes(data);
            } finally {
                buffer.release();
            }
            if (pending.addAndGet(data.length) > PAUSE_READING_BYTES) {
                channel.config().setAutoRead(false);
            }
            queue.execute(() -> {
                try {
                    guard(() -> handler.received(this, data));
                } finally {
                    long left = pending.addAndGet(-data.length);
                    if (left < RESUME_READING_BYTES && left + data.length >= RESUME_READING_BYTES && !channel.eventLoop().isShuttingDown()) {
                        channel.eventLoop().execute(this::resumeReading);
                    }
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext context) {
            queue.execute(() -> guard(() -> handler.closed(this)));
            context.fireChannelInactive();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            LOGGER.debug("Closing {} after an error", context.channel().remoteAddress(), cause);
            context.close();
        }

        private void resumeReading() {
            if (pending.get() < RESUME_READING_BYTES && !channel.config().isAutoRead()) {
                channel.config().setAutoRead(true);
            }
        }

        private void guard(Runnable action) {
            try {
                action.run();
            } catch (RuntimeException | LinkageError e) {
                LOGGER.warn("A TCP handler failed for {}", channel.remoteAddress(), e);
                channel.close();
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
