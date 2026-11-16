package org.bukkit.entity;

public interface Tameable extends org.bukkit.entity.Animals {

    boolean isTamed();

    void setTamed(boolean p0);

    org.bukkit.entity.AnimalTamer getOwner();

    void setOwner(org.bukkit.entity.AnimalTamer p0);
}
