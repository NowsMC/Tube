package space.nows.tube.api.scheduler;

import space.nows.tube.api.entity.Entity;

import java.util.concurrent.atomic.AtomicReference;

final class SchedulerEntities implements EntityScheduler {

    private final Scheduler scheduler;
    private final Entity entity;

    SchedulerEntities(Scheduler scheduler, Entity entity) {
        this.scheduler = scheduler;
        this.entity = entity;
    }

    @Override
    public ScheduledTask run(Runnable task, Runnable retired) {
        return scheduler.runSync(() -> runOrRetire(task, retired));
    }

    @Override
    public ScheduledTask runLater(long delayTicks, Runnable task, Runnable retired) {
        return scheduler.runLater(delayTicks, () -> runOrRetire(task, retired));
    }

    @Override
    public ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task, Runnable retired) {
        AtomicReference<ScheduledTask> self = new AtomicReference<>();
        ScheduledTask scheduled = scheduler.runRepeating(delayTicks, periodTicks, () -> {
            if (runOrRetire(task, retired) && self.get() != null) {
                self.get().cancel();
            }
        });
        self.set(scheduled);
        return scheduled;
    }

    private boolean runOrRetire(Runnable task, Runnable retired) {
        if (!entity.isRemoved()) {
            task.run();
            return false;
        }
        if (retired != null) {
            retired.run();
        }
        return true;
    }
}
