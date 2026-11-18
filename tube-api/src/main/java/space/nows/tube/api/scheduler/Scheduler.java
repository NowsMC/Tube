package space.nows.tube.api.scheduler;

import space.nows.tube.api.entity.Entity;

public interface Scheduler {

    ScheduledTask runSync(Runnable task);

    ScheduledTask runLater(long delayTicks, Runnable task);

    ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task);

    default RegionScheduler region() {
        return new SchedulerRegions(this);
    }

    default EntityScheduler entity(Entity entity) {
        return new SchedulerEntities(this, entity);
    }
}
