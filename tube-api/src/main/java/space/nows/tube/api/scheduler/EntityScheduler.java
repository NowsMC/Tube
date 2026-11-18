package space.nows.tube.api.scheduler;

public interface EntityScheduler {

    ScheduledTask run(Runnable task, Runnable retired);

    ScheduledTask runLater(long delayTicks, Runnable task, Runnable retired);

    ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task, Runnable retired);
}
