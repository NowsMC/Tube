package org.bukkit.inventory.meta;

public interface FireworkMeta extends org.bukkit.inventory.meta.ItemMeta {


    void clearEffects();

    boolean hasEffects();

    int getPower();

    void setPower(int p0);

    org.bukkit.inventory.meta.FireworkMeta clone();
}
