package space.nows.tube.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketAddress;

final class Binding {

    private Binding() {
    }

    static Channel await(ChannelFuture bind, SocketAddress address) {
        bind.awaitUninterruptibly();
        if (bind.isSuccess()) {
            return bind.channel();
        }
        Throwable cause = bind.cause();
        IOException failure = cause instanceof IOException io ? io : new IOException(cause == null ? "binding was cancelled" : cause.toString(), cause);
        throw new UncheckedIOException("Could not bind " + address + ": " + failure.getMessage(), failure);
    }
}
