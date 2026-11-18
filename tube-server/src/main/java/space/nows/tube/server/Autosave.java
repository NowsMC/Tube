package space.nows.tube.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class Autosave implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final ScheduledExecutorService executor;

    private Autosave(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    static Autosave start(int intervalSeconds, AutosaveGate gate, Runnable saveWorlds, Runnable savePlayersOnTick) {
        if (intervalSeconds <= 0) {
            return new Autosave(null);
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "tube-autosave");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(() -> {
            if (!gate.enabled()) {
                return;
            }
            try {
                saveWorlds.run();
                savePlayersOnTick.run();
            } catch (RuntimeException e) {
                LOGGER.error("Autosave failed", e);
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        return new Autosave(executor);
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
