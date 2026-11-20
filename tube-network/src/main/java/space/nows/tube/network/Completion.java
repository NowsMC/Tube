package space.nows.tube.network;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

final class Completion {

    private Completion() {
    }

    static CompletableFuture<Void> of(ChannelFuture write, Executor callbacks) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        write.addListener(done -> {
            Runnable finish = done.isSuccess() ? () -> result.complete(null) : () -> result.completeExceptionally(done.cause());
            try {
                callbacks.execute(finish);
            } catch (RejectedExecutionException e) {
                finish.run();
            }
        });
        return result;
    }
}
