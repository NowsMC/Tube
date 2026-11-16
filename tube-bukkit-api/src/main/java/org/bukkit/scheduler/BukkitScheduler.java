package org.bukkit.scheduler;

import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface BukkitScheduler {

    int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay);

    int scheduleSyncDelayedTask(Plugin plugin, Runnable task);

    int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period);

    <T> Future<T> callSyncMethod(Plugin plugin, Callable<T> task);

    void cancelTask(int taskId);

    void cancelTasks(Plugin plugin);

    boolean isCurrentlyRunning(int taskId);

    boolean isQueued(int taskId);

    List<BukkitTask> getPendingTasks();

    BukkitTask runTask(Plugin plugin, Runnable task);

    void runTask(Plugin plugin, Consumer<BukkitTask> task);

    BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task);

    void runTaskAsynchronously(Plugin plugin, Consumer<BukkitTask> task);

    BukkitTask runTaskLater(Plugin plugin, Runnable task, long delay);

    void runTaskLater(Plugin plugin, Consumer<BukkitTask> task, long delay);

    BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay);

    void runTaskLaterAsynchronously(Plugin plugin, Consumer<BukkitTask> task, long delay);

    BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period);

    void runTaskTimer(Plugin plugin, Consumer<BukkitTask> task, long delay, long period);

    BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period);

    void runTaskTimerAsynchronously(Plugin plugin, Consumer<BukkitTask> task, long delay, long period);
}
