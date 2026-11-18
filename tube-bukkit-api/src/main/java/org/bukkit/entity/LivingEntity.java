package org.bukkit.entity;

public interface LivingEntity extends Damageable, org.bukkit.attribute.Attributable {

    org.bukkit.Location getEyeLocation();

    double getEyeHeight();

    boolean isSwimming();

    boolean isGliding();

    boolean hasLineOfSight(Entity other);

    boolean addPotionEffect(org.bukkit.potion.PotionEffect effect);

    boolean hasPotionEffect(org.bukkit.potion.PotionEffectType type);

    org.bukkit.potion.PotionEffect getPotionEffect(org.bukkit.potion.PotionEffectType type);

    void removePotionEffect(org.bukkit.potion.PotionEffectType type);

    java.util.Collection<org.bukkit.potion.PotionEffect> getActivePotionEffects();
}
