package org.bukkit.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class BukkitRunnable implements Runnable {

    private BukkitTask task;

    public synchronized boolean isCancelled() {
        checkScheduled();
        return task.isCancelled();
    }

    public synchronized void cancel() {
        Bukkit.getScheduler().cancelTask(getTaskId());
    }

    public synchronized BukkitTask runTask(Plugin plugin) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTask(plugin, (Runnable) this);
    }

    public synchronized BukkitTask runTaskAsynchronously(Plugin plugin) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTaskAsynchronously(plugin, (Runnable) this);
    }

    public synchronized BukkitTask runTaskLater(Plugin plugin, long delay) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTaskLater(plugin, (Runnable) this, delay);
    }

    public synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, (Runnable) this, delay);
    }

    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTaskTimer(plugin, (Runnable) this, delay, period);
    }

    public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, (Runnable) this, delay, period);
    }

    public synchronized int getTaskId() {
        checkScheduled();
        return task.getTaskId();
    }

    private void checkScheduled() {
        if (task == null) {
            throw new IllegalStateException("Not scheduled yet");
        }
    }

    private void checkNotYetScheduled() {
        if (task != null) {
            throw new IllegalStateException("Already scheduled as " + task.getTaskId());
        }
    }
}
