package org.bukkit.entity;

public interface Projectile extends org.bukkit.entity.Entity {

    org.bukkit.projectiles.ProjectileSource getShooter();

    void setShooter(org.bukkit.projectiles.ProjectileSource p0);
}
