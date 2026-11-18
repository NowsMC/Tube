package space.nows.tube.api.scheduler;

import space.nows.tube.api.world.World;

final class SchedulerRegions implements RegionScheduler {

    private final Scheduler scheduler;

    SchedulerRegions(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public ScheduledTask run(World world, int chunkX, int chunkZ, Runnable task) {
        return scheduler.runSync(task);
    }

    @Override
    public ScheduledTask runLater(World world, int chunkX, int chunkZ, long delayTicks, Runnable task) {
        return scheduler.runLater(delayTicks, task);
    }

    @Override
    public ScheduledTask runRepeating(World world, int chunkX, int chunkZ, long delayTicks, long periodTicks, Runnable task) {
        return scheduler.runRepeating(delayTicks, periodTicks, task);
    }
}
