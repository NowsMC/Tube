package org.bukkit.entity;

public interface AreaEffectCloud extends org.bukkit.entity.Entity {

    org.bukkit.potion.PotionType getBasePotionType();

    boolean hasCustomEffects();

    java.util.List<org.bukkit.potion.PotionEffect> getCustomEffects();

    org.bukkit.projectiles.ProjectileSource getSource();
}
