package org.bukkit.event.entity;

public class TrialSpawnerSpawnEvent extends org.bukkit.event.entity.EntitySpawnEvent {
    private org.bukkit.block.TrialSpawner spawner;

    public TrialSpawnerSpawnEvent(org.bukkit.entity.Entity p0, org.bukkit.block.TrialSpawner p1) {
        super(p0);
        this.spawner = p1;
    }

    public org.bukkit.block.TrialSpawner getTrialSpawner() {
        return spawner;
    }
}
