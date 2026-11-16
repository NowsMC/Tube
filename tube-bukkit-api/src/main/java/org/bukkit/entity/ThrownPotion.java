package org.bukkit.entity;

public interface ThrownPotion extends org.bukkit.entity.ThrowableProjectile {

    java.util.Collection<org.bukkit.potion.PotionEffect> getEffects();

    org.bukkit.inventory.ItemStack getItem();
}
