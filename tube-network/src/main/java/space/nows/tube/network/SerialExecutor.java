package space.nows.tube.network;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class SerialExecutor implements Executor {

    private final Executor backing;
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final AtomicInteger queued = new AtomicInteger();
    private final AtomicBoolean running = new AtomicBoolean();

    SerialExecutor(Executor backing) {
        this.backing = backing;
    }

    @Override
    public void execute(Runnable task) {
        queued.incrementAndGet();
        tasks.add(task);
        schedule();
    }

    int queued() {
        return queued.get();
    }

    private void schedule() {
        if (running.compareAndSet(false, true)) {
            try {
                backing.execute(this::drain);
            } catch (RejectedExecutionException e) {
                tasks.clear();
                queued.set(0);
                running.set(false);
            }
        }
    }

    private void drain() {
        try {
            Runnable task;
            while ((task = tasks.poll()) != null) {
                try {
                    task.run();
                } finally {
                    queued.decrementAndGet();
                }
            }
        } finally {
            running.set(false);
            if (!tasks.isEmpty()) {
                schedule();
            }
        }
    }
}
