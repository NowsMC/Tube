package space.nows.tube.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.ScheduledTask;
import space.nows.tube.api.Scheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

final class TubeBukkitScheduler implements BukkitScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-bukkit");
    private static final long TICK_MILLIS = 50;

    private static final class Task implements BukkitTask {

        private final int id;
        private final Plugin owner;
        private final boolean sync;
        private volatile boolean cancelled;
        private volatile Runnable canceller = () -> { };

        Task(int id, Plugin owner, boolean sync) {
            this.id = id;
            this.owner = owner;
            this.sync = sync;
        }

        @Override
        public int getTaskId() {
            return id;
        }

        @Override
        public Plugin getOwner() {
            return owner;
        }

        @Override
        public boolean isSync() {
            return sync;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void cancel() {
            cancelled = true;
            canceller.run();
        }
    }

    private final Scheduler tube;
    private final AtomicInteger ids = new AtomicInteger(1);
    private final Map<Integer, Task> tasks = new java.util.concurrent.ConcurrentHashMap<>();
    private final ScheduledExecutorService asyncPool;

    TubeBukkitScheduler(Scheduler tube) {
        this.tube = tube;
        AtomicLong threads = new AtomicLong();
        this.asyncPool = Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "Tube Bukkit Scheduler Thread - " + threads.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    void shutdown() {
        tasks.values().forEach(Task::cancel);
        asyncPool.shutdownNow();
    }

    private Task schedule(Plugin plugin, Consumer<BukkitTask> body, long delay, long period, boolean async) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (!plugin.isEnabled()) {
            throw new org.bukkit.plugin.IllegalPluginAccessException("Plugin attempted to register task while disabled");
        }
        Task task = new Task(ids.getAndIncrement(), plugin, !async);
        tasks.put(task.id, task);
        boolean repeating = period > 0;
        Runnable work = () -> {
            if (task.cancelled) {
                return;
            }
            try {
                body.accept(task);
            } catch (Throwable error) {
                LOGGER.error("Task #{} for {} generated an exception", task.id, plugin.getName(), error);
            } finally {
                if (!repeating) {
                    tasks.remove(task.id);
                }
            }
        };
        long safeDelay = Math.max(0, delay);
        if (async) {
            ScheduledFuture<?> future = repeating
                    ? asyncPool.scheduleAtFixedRate(work, safeDelay * TICK_MILLIS, period * TICK_MILLIS, TimeUnit.MILLISECONDS)
                    : asyncPool.schedule(work, safeDelay * TICK_MILLIS, TimeUnit.MILLISECONDS);
            task.canceller = () -> {
                future.cancel(false);
                tasks.remove(task.id);
            };
        } else {
            ScheduledTask handle = repeating ? tube.runRepeating(safeDelay, period, work) : tube.runLater(safeDelay, work);
            task.canceller = () -> {
                handle.cancel();
                tasks.remove(task.id);
            };
        }
        return task;
    }

    private static Consumer<BukkitTask> run(Runnable runnable) {
        return task -> runnable.run();
    }

    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay) {
        return schedule(plugin, run(task), delay, -1, false).id;
    }

    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task) {
        return schedule(plugin, run(task), 0, -1, false).id;
    }

    @Override
    public int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
        return schedule(plugin, run(task), delay, period, false).id;
    }

    @Override
    public <T> Future<T> callSyncMethod(Plugin plugin, Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        schedule(plugin, bukkitTask -> {
            try {
                future.complete(task.call());
            } catch (Throwable error) {
                future.completeExceptionally(error);
            }
        }, 0, -1, false);
        return future;
    }

    @Override
    public void cancelTask(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        tasks.values().stream().filter(task -> task.owner == plugin).toList().forEach(Task::cancel);
    }

    @Override
    public boolean isCurrentlyRunning(int taskId) {
        return false;
    }

    @Override
    public boolean isQueued(int taskId) {
        Task task = tasks.get(taskId);
        return task != null && !task.cancelled;
    }

    @Override
    public List<BukkitTask> getPendingTasks() {
        return List.copyOf(tasks.values());
    }

    @Override
    public BukkitTask runTask(Plugin plugin, Runnable task) {
        return schedule(plugin, run(task), 0, -1, false);
    }

    @Override
    public void runTask(Plugin plugin, Consumer<BukkitTask> task) {
        schedule(plugin, task, 0, -1, false);
    }

    @Override
    public BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task) {
        return schedule(plugin, run(task), 0, -1, true);
    }

    @Override
    public void runTaskAsynchronously(Plugin plugin, Consumer<BukkitTask> task) {
        schedule(plugin, task, 0, -1, true);
    }

    @Override
    public BukkitTask runTaskLater(Plugin plugin, Runnable task, long delay) {
        return schedule(plugin, run(task), delay, -1, false);
    }

    @Override
    public void runTaskLater(Plugin plugin, Consumer<BukkitTask> task, long delay) {
        schedule(plugin, task, delay, -1, false);
    }

    @Override
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        return schedule(plugin, run(task), delay, -1, true);
    }

    @Override
    public void runTaskLaterAsynchronously(Plugin plugin, Consumer<BukkitTask> task, long delay) {
        schedule(plugin, task, delay, -1, true);
    }

    @Override
    public BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        return schedule(plugin, run(task), delay, period, false);
    }

    @Override
    public void runTaskTimer(Plugin plugin, Consumer<BukkitTask> task, long delay, long period) {
        schedule(plugin, task, delay, period, false);
    }

    @Override
    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
        return schedule(plugin, run(task), delay, period, true);
    }

    @Override
    public void runTaskTimerAsynchronously(Plugin plugin, Consumer<BukkitTask> task, long delay, long period) {
        schedule(plugin, task, delay, period, true);
    }
}
