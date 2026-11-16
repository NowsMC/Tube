package space.nows.tube.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.scheduler.Scheduler;

import java.util.function.LongSupplier;

final class TickWatchdog implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final long maxTickMillis;
    private final LongSupplier clock;
    private final Runnable halt;
    private volatile long lastTick;
    private volatile Thread tickThread;
    private volatile boolean running = true;

    TickWatchdog(long maxTickMillis, LongSupplier clock, Runnable halt) {
        this.maxTickMillis = maxTickMillis;
        this.clock = clock;
        this.halt = halt;
        this.lastTick = clock.getAsLong();
    }

    static TickWatchdog start(int maxTickMillis, Scheduler scheduler) {
        TickWatchdog watchdog = new TickWatchdog(maxTickMillis, System::currentTimeMillis, () -> Runtime.getRuntime().halt(1));
        if (maxTickMillis <= 0) {
            return watchdog;
        }
        scheduler.runRepeating(0, 1, watchdog::tick);
        Thread thread = new Thread(watchdog::watch, "tube-watchdog");
        thread.setDaemon(true);
        thread.start();
        return watchdog;
    }

    void tick() {
        tickThread = Thread.currentThread();
        lastTick = clock.getAsLong();
    }

    boolean isStalled() {
        return clock.getAsLong() - lastTick > maxTickMillis;
    }

    private void watch() {
        while (running) {
            if (isStalled()) {
                report();
                halt.run();
                return;
            }
            try {
                Thread.sleep(Math.min(1000, maxTickMillis));
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void report() {
        LOGGER.error("A single server tick took {} seconds (should be max {} seconds)",
                String.format("%.2f", (clock.getAsLong() - lastTick) / 1000.0), String.format("%.2f", maxTickMillis / 1000.0));
        LOGGER.error("Considering it to be crashed, server will forcibly shutdown.");
        Thread thread = tickThread;
        if (thread != null) {
            StringBuilder trace = new StringBuilder("Tick thread ").append(thread.getName()).append(" is at:");
            for (StackTraceElement element : thread.getStackTrace()) {
                trace.append(System.lineSeparator()).append("\tat ").append(element);
            }
            LOGGER.error(trace.toString());
        }
    }

    @Override
    public void close() {
        running = false;
    }
}
