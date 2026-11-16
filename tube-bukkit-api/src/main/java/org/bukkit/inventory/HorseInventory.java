package org.bukkit.inventory;

public interface HorseInventory extends org.bukkit.inventory.AbstractHorseInventory {

    org.bukkit.inventory.ItemStack getSaddle();

    void setSaddle(org.bukkit.inventory.ItemStack p0);

    org.bukkit.inventory.ItemStack getArmor();

    void setArmor(org.bukkit.inventory.ItemStack p0);
}
