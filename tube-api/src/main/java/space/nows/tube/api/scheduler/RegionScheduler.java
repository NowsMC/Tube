package space.nows.tube.api.scheduler;

import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;

public interface RegionScheduler {

    ScheduledTask run(World world, int chunkX, int chunkZ, Runnable task);

    ScheduledTask runLater(World world, int chunkX, int chunkZ, long delayTicks, Runnable task);

    ScheduledTask runRepeating(World world, int chunkX, int chunkZ, long delayTicks, long periodTicks, Runnable task);

    default ScheduledTask run(Location at, Runnable task) {
        return run(at.world(), chunkOf(at.x()), chunkOf(at.z()), task);
    }

    default ScheduledTask runLater(Location at, long delayTicks, Runnable task) {
        return runLater(at.world(), chunkOf(at.x()), chunkOf(at.z()), delayTicks, task);
    }

    default ScheduledTask runRepeating(Location at, long delayTicks, long periodTicks, Runnable task) {
        return runRepeating(at.world(), chunkOf(at.x()), chunkOf(at.z()), delayTicks, periodTicks, task);
    }

    private static int chunkOf(double coordinate) {
        return (int) Math.floor(coordinate) >> 4;
    }
}
