package org.bukkit.entity;

public interface Creeper extends org.bukkit.entity.Monster {

    boolean isPowered();

    void setPowered(boolean p0);

    org.bukkit.entity.Entity getIgniter();
}
