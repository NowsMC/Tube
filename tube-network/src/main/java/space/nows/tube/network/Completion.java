package space.nows.tube.network;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.CompletableFuture;

final class Completion {

    private Completion() {
    }

    static CompletableFuture<Void> of(ChannelFuture write) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        write.addListener(done -> {
            if (done.isSuccess()) {
                result.complete(null);
            } else {
                result.completeExceptionally(done.cause());
            }
        });
        return result;
    }
}
