package space.nows.tube.server;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import space.nows.tube.api.TickReport;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

final class TickTracker {

    private static final int TICKS_PER_SECOND = 20;
    private static final int KEPT = TICKS_PER_SECOND * 300;
    private static final int AVERAGED = 100;

    private final LongSupplier clock;
    private final long[] finishedAt = new long[KEPT];
    private final double[] durations = new double[KEPT];
    private final long startedAt;
    private int recorded;

    TickTracker(LongSupplier clock) {
        this.clock = clock;
        this.startedAt = clock.getAsLong();
    }

    void install(GlobalEventHandler handler) {
        handler.addListener(ServerTickMonitorEvent.class, event -> record(event.getTickMonitor().getTickTime()));
    }

    synchronized void record(double millis) {
        int slot = recorded % KEPT;
        finishedAt[slot] = clock.getAsLong();
        durations[slot] = millis;
        recorded++;
    }

    synchronized TickReport report() {
        long now = clock.getAsLong();
        return new TickReport(perSecond(now, 5), perSecond(now, 60), perSecond(now, 300), averageMillis());
    }

    private double perSecond(long now, int seconds) {
        long window = TimeUnit.SECONDS.toNanos(seconds);
        long covered = Math.min(window, now - startedAt);
        if (covered <= 0) {
            return TICKS_PER_SECOND;
        }
        int ticks = 0;
        for (int i = 0; i < Math.min(recorded, KEPT); i++) {
            if (now - finishedAt[i] <= window) {
                ticks++;
            }
        }
        return Math.min(TICKS_PER_SECOND, ticks / (covered / 1e9));
    }

    private double averageMillis() {
        int count = Math.min(recorded, AVERAGED);
        if (count == 0) {
            return 0;
        }
        double total = 0;
        for (int i = 1; i <= count; i++) {
            total += durations[Math.floorMod(recorded - i, KEPT)];
        }
        return total / count;
    }
}
