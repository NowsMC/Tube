package space.nows.tube.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class RconServer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-rcon");

    private static final int TYPE_RESPONSE = 0;
    private static final int TYPE_COMMAND = 2;
    private static final int TYPE_LOGIN = 3;
    private static final int MAX_FRAME = 1460;
    private static final int MAX_RESPONSE = 4096;
    private static final int LOGIN_TIMEOUT_SECONDS = 30;

    private final Channel channel;
    private final LoginGuard guard = new LoginGuard(System::currentTimeMillis);

    public RconServer(NettyNetwork network, InetSocketAddress address, String password, Function<String, CompletableFuture<String>> executor) {
        byte[] expected = password.getBytes(StandardCharsets.UTF_8);
        this.channel = Binding.await(new ServerBootstrap()
                .group(network.group())
                .channel(network.transport().serverChannel())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel child) {
                        if (guard.locked(child.remoteAddress().getAddress())) {
                            LOGGER.warn("RCON client {} is locked out after failed logins", child.remoteAddress());
                            child.close();
                            return;
                        }
                        child.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, MAX_FRAME, 0, 4, 0, 4, true))
                                .addLast(new Session(expected, executor, guard));
                    }
                })
                .bind(address), address);
    }

    public InetSocketAddress address() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    public void close() {
        channel.close().syncUninterruptibly();
    }

    private static final class Session extends SimpleChannelInboundHandler<ByteBuf> {

        private final byte[] password;
        private final Function<String, CompletableFuture<String>> executor;
        private final LoginGuard guard;
        private boolean authenticated;
        private ScheduledFuture<?> loginTimeout;

        Session(byte[] password, Function<String, CompletableFuture<String>> executor, LoginGuard guard) {
            this.password = password;
            this.executor = executor;
            this.guard = guard;
        }

        @Override
        public void channelActive(ChannelHandlerContext context) {
            loginTimeout = context.executor().schedule(() -> {
                if (!authenticated) {
                    LOGGER.debug("RCON client {} did not log in in time", context.channel().remoteAddress());
                    context.close();
                }
            }, LOGIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            context.fireChannelActive();
        }

        @Override
        public void channelInactive(ChannelHandlerContext context) {
            if (loginTimeout != null) {
                loginTimeout.cancel(false);
            }
            context.fireChannelInactive();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext context, ByteBuf frame) {
            if (frame.readableBytes() < 10) {
                context.close();
                return;
            }
            int id = frame.readIntLE();
            int type = frame.readIntLE();
            byte[] raw = new byte[frame.readableBytes() - 2];
            frame.readBytes(raw);
            String payload = new String(raw, StandardCharsets.UTF_8);

            if (type == TYPE_LOGIN) {
                InetAddress address = ((InetSocketAddress) context.channel().remoteAddress()).getAddress();
                if (guard.locked(address)) {
                    respond(context, -1, TYPE_COMMAND, "");
                    context.close();
                    return;
                }
                if (MessageDigest.isEqual(password, raw)) {
                    authenticated = true;
                    guard.succeeded(address);
                    loginTimeout.cancel(false);
                    LOGGER.info("RCON client {} logged in", context.channel().remoteAddress());
                    respond(context, id, TYPE_COMMAND, "");
                } else {
                    LOGGER.warn("RCON client {} failed to log in", context.channel().remoteAddress());
                    guard.failed(address);
                    respond(context, -1, TYPE_COMMAND, "");
                    context.close();
                }
                return;
            }
            if (!authenticated) {
                respond(context, -1, TYPE_COMMAND, "");
                context.close();
                return;
            }
            if (type != TYPE_COMMAND) {
                respond(context, id, TYPE_RESPONSE, "Unknown request " + Integer.toHexString(type));
                return;
            }
            executor.apply(payload.strip()).whenComplete((output, error) -> {
                String text = error == null ? output : "Error executing: " + payload;
                context.executor().execute(() -> respond(context, id, TYPE_RESPONSE, text == null ? "" : text));
            });
        }

        private static void respond(ChannelHandlerContext context, int id, int type, String text) {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            int offset = 0;
            do {
                int length = Math.min(MAX_RESPONSE, bytes.length - offset);
                ByteBuf out = Unpooled.buffer(14 + length);
                out.writeIntLE(10 + length);
                out.writeIntLE(id);
                out.writeIntLE(type);
                out.writeBytes(bytes, offset, length);
                out.writeByte(0);
                out.writeByte(0);
                context.write(out);
                offset += length;
            } while (offset < bytes.length);
            context.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            LOGGER.debug("Closing RCON client {}", context.channel().remoteAddress(), cause);
            context.close();
        }
    }
}
