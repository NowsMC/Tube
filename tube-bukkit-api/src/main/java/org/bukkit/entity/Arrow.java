package org.bukkit.entity;

public interface Arrow extends org.bukkit.entity.AbstractArrow {

    org.bukkit.potion.PotionType getBasePotionType();

    java.util.List<org.bukkit.potion.PotionEffect> getCustomEffects();
}
