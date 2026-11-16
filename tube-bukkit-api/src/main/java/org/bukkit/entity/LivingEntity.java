package org.bukkit.entity;

public interface LivingEntity extends Damageable {

    boolean addPotionEffect(org.bukkit.potion.PotionEffect effect);

    boolean hasPotionEffect(org.bukkit.potion.PotionEffectType type);

    org.bukkit.potion.PotionEffect getPotionEffect(org.bukkit.potion.PotionEffectType type);

    void removePotionEffect(org.bukkit.potion.PotionEffectType type);

    java.util.Collection<org.bukkit.potion.PotionEffect> getActivePotionEffects();
}
