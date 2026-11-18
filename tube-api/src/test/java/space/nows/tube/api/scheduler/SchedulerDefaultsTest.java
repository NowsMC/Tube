package space.nows.tube.api.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import space.nows.tube.api.entity.Entity;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class SchedulerDefaultsTest {

    private static final class Manual implements Scheduler {

        final List<Runnable> now = new ArrayList<>();
        final List<Runnable> repeating = new ArrayList<>();
        final List<String> log = new ArrayList<>();
        boolean cancelled;

        private ScheduledTask task() {
            return new ScheduledTask() {
                @Override
                public void cancel() {
                    cancelled = true;
                }

                @Override
                public boolean isActive() {
                    return !cancelled;
                }
            };
        }

        @Override
        public ScheduledTask runSync(Runnable task) {
            log.add("sync");
            now.add(task);
            return task();
        }

        @Override
        public ScheduledTask runLater(long delayTicks, Runnable task) {
            log.add("later " + delayTicks);
            now.add(task);
            return task();
        }

        @Override
        public ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task) {
            log.add("repeat " + delayTicks + " " + periodTicks);
            repeating.add(task);
            return task();
        }
    }

    private static Entity entity(AtomicBoolean removed) {
        return (Entity) Proxy.newProxyInstance(Entity.class.getClassLoader(), new Class<?>[] {Entity.class}, (proxy, method, args) -> method.getName().equals("isRemoved") ? removed.get() : null);
    }

    @Test
    void regionTasksUseTheMainScheduler() {
        Manual scheduler = new Manual();
        RegionScheduler regions = scheduler.region();
        List<String> ran = new ArrayList<>();

        regions.run(null, 0, 0, () -> ran.add("a"));
        regions.runLater(null, 1, 2, 10, () -> ran.add("b"));
        regions.runRepeating(null, 0, 0, 5, 20, () -> ran.add("c"));
        scheduler.now.forEach(Runnable::run);
        scheduler.repeating.forEach(Runnable::run);

        assertEquals(List.of("sync", "later 10", "repeat 5 20"), scheduler.log);
        assertEquals(List.of("a", "b", "c"), ran);
    }

    @Test
    void entityTasksRunWhileTheEntityLivesAndRetireWhenItIsGone() {
        Manual scheduler = new Manual();
        AtomicBoolean removed = new AtomicBoolean();
        List<String> ran = new ArrayList<>();
        EntityScheduler entities = scheduler.entity(entity(removed));

        entities.run(() -> ran.add("once"), () -> ran.add("retired once"));
        entities.runRepeating(1, 2, () -> ran.add("tick"), () -> ran.add("retired repeat"));
        scheduler.now.forEach(Runnable::run);
        scheduler.repeating.forEach(Runnable::run);
        removed.set(true);
        scheduler.repeating.forEach(Runnable::run);

        assertEquals(List.of("once", "tick", "retired repeat"), ran);
        assertTrue(scheduler.cancelled);
    }

    @Test
    void aRemovedEntityRetiresASingleTask() {
        Manual scheduler = new Manual();
        AtomicBoolean removed = new AtomicBoolean(true);
        List<String> ran = new ArrayList<>();

        scheduler.entity(entity(removed)).runLater(5, () -> ran.add("task"), () -> ran.add("retired"));
        scheduler.now.forEach(Runnable::run);

        assertEquals(List.of("retired"), ran);
    }
}
