package org.bukkit.inventory.meta;

public interface SuspiciousStewMeta extends org.bukkit.inventory.meta.ItemMeta {

    boolean hasCustomEffects();

    java.util.List<org.bukkit.potion.PotionEffect> getCustomEffects();

    boolean addCustomEffect(org.bukkit.potion.PotionEffect p0, boolean p1);

    boolean clearCustomEffects();

    org.bukkit.inventory.meta.SuspiciousStewMeta clone();
}
