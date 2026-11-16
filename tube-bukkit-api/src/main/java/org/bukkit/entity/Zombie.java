package org.bukkit.entity;

public interface Zombie extends org.bukkit.entity.Monster, org.bukkit.entity.Ageable {

    boolean isBaby();

    void setBaby(boolean p0);

    void setVillager(boolean p0);
}
