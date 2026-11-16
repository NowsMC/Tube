package org.bukkit.inventory.meta;

public interface PotionMeta extends org.bukkit.inventory.meta.ItemMeta {

    void setBasePotionType(org.bukkit.potion.PotionType p0);

    org.bukkit.potion.PotionType getBasePotionType();

    boolean hasCustomEffects();

    java.util.List<org.bukkit.potion.PotionEffect> getCustomEffects();

    boolean addCustomEffect(org.bukkit.potion.PotionEffect p0, boolean p1);

    boolean clearCustomEffects();

    boolean hasColor();

    org.bukkit.inventory.meta.PotionMeta clone();
}
