package org.bukkit.inventory;

public interface EntityEquipment {

    void setItem(org.bukkit.inventory.EquipmentSlot p0, org.bukkit.inventory.ItemStack p1);

    org.bukkit.inventory.ItemStack getItem(org.bukkit.inventory.EquipmentSlot p0);

    org.bukkit.inventory.ItemStack getItemInMainHand();

    void setItemInMainHand(org.bukkit.inventory.ItemStack p0);

    org.bukkit.inventory.ItemStack getItemInOffHand();

    void setItemInOffHand(org.bukkit.inventory.ItemStack p0);

    void setItemInHand(org.bukkit.inventory.ItemStack p0);

    void setHelmet(org.bukkit.inventory.ItemStack p0);

    void setChestplate(org.bukkit.inventory.ItemStack p0);

    void setLeggings(org.bukkit.inventory.ItemStack p0);

    void setBoots(org.bukkit.inventory.ItemStack p0);

    org.bukkit.inventory.ItemStack[] getArmorContents();

    void setArmorContents(org.bukkit.inventory.ItemStack[] p0);

    void clear();

    void setItemInHandDropChance(float p0);

    void setItemInMainHandDropChance(float p0);

    void setHelmetDropChance(float p0);

    void setChestplateDropChance(float p0);

    void setLeggingsDropChance(float p0);

    void setBootsDropChance(float p0);

    org.bukkit.entity.Entity getHolder();
}
