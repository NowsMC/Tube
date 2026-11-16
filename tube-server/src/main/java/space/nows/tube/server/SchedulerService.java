package space.nows.tube.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.api.ScheduledTask;
import space.nows.tube.api.Scheduler;

final class SchedulerService implements Scheduler {

    @Override
    public ScheduledTask runSync(Runnable task) {
        return wrap(MinecraftServer.getSchedulerManager().scheduleNextTick(task));
    }

    @Override
    public ScheduledTask runLater(long delayTicks, Runnable task) {
        requireNonNegative(delayTicks, "delayTicks");
        return wrap(MinecraftServer.getSchedulerManager().buildTask(task)
                .delay(ticks(delayTicks))
                .schedule());
    }

    @Override
    public ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task) {
        requireNonNegative(delayTicks, "delayTicks");
        if (periodTicks < 1) {
            throw new IllegalArgumentException("periodTicks must be at least 1");
        }
        return wrap(MinecraftServer.getSchedulerManager().buildTask(task)
                .delay(ticks(delayTicks))
                .repeat(ticks(periodTicks))
                .schedule());
    }

    private static TaskSchedule ticks(long ticks) {
        return ticks == 0 ? TaskSchedule.immediate() : TaskSchedule.tick(Math.toIntExact(ticks));
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }

    private static ScheduledTask wrap(Task task) {
        return new ScheduledTask() {
            @Override
            public void cancel() {
                task.cancel();
            }

            @Override
            public boolean isActive() {
                return task.isAlive();
            }
        };
    }
}
