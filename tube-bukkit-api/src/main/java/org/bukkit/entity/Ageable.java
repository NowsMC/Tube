package org.bukkit.entity;

public interface Ageable extends org.bukkit.entity.Creature {

    int getAge();

    void setAge(int p0);

    void setAgeLock(boolean p0);

    boolean getAgeLock();

    void setBaby();

    void setAdult();

    boolean isAdult();

    boolean canBreed();

    void setBreed(boolean p0);
}
