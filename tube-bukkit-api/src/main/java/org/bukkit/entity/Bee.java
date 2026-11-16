package org.bukkit.entity;

public interface Bee extends org.bukkit.entity.Animals {

    boolean hasNectar();

    void setHasNectar(boolean p0);

    boolean hasStung();

    void setHasStung(boolean p0);

    int getAnger();

    void setAnger(int p0);
}
