package org.bukkit.entity;

public interface Damageable extends Entity {

    void damage(double amount);

    double getHealth();

    void setHealth(double health);

    double getMaxHealth();
}
