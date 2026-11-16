package org.bukkit.entity;

public interface LivingEntity extends Entity {

    double getHealth();

    void setHealth(double health);

    double getMaxHealth();

    boolean addPotionEffect(org.bukkit.potion.PotionEffect effect);

    boolean hasPotionEffect(org.bukkit.potion.PotionEffectType type);

    org.bukkit.potion.PotionEffect getPotionEffect(org.bukkit.potion.PotionEffectType type);

    void removePotionEffect(org.bukkit.potion.PotionEffectType type);

    java.util.Collection<org.bukkit.potion.PotionEffect> getActivePotionEffects();
}
