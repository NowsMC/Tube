package space.nows.tube.server;

import space.nows.tube.api.ScheduledTask;
import space.nows.tube.api.Scheduler;

import java.util.ArrayList;
import java.util.List;

final class ScopedScheduler implements Scheduler {

    private final Scheduler delegate;
    private final List<ScheduledTask> tasks = new ArrayList<>();

    ScopedScheduler(Scheduler delegate) {
        this.delegate = delegate;
    }

    @Override
    public ScheduledTask runSync(Runnable task) {
        return track(delegate.runSync(task));
    }

    @Override
    public ScheduledTask runLater(long delayTicks, Runnable task) {
        return track(delegate.runLater(delayTicks, task));
    }

    @Override
    public ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task) {
        return track(delegate.runRepeating(delayTicks, periodTicks, task));
    }

    synchronized void cancelAll() {
        tasks.forEach(ScheduledTask::cancel);
        tasks.clear();
    }

    private synchronized ScheduledTask track(ScheduledTask task) {
        tasks.removeIf(existing -> !existing.isActive());
        tasks.add(task);
        return task;
    }
}
