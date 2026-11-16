package space.nows.tube.api;

public interface Scheduler {

    ScheduledTask runSync(Runnable task);

    ScheduledTask runLater(long delayTicks, Runnable task);

    ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task);
}
