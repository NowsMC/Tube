package org.bukkit.inventory;

import org.bukkit.entity.HumanEntity;

public interface PlayerInventory extends Inventory {

    ItemStack[] getArmorContents();

    void setArmorContents(ItemStack[] items);

    ItemStack getHelmet();

    void setHelmet(ItemStack item);

    ItemStack getChestplate();

    void setChestplate(ItemStack item);

    ItemStack getLeggings();

    void setLeggings(ItemStack item);

    ItemStack getBoots();

    void setBoots(ItemStack item);

    ItemStack getItemInMainHand();

    void setItemInMainHand(ItemStack item);

    ItemStack getItemInOffHand();

    void setItemInOffHand(ItemStack item);

    ItemStack getItemInHand();

    void setItemInHand(ItemStack item);

    ItemStack getItem(EquipmentSlot slot);

    void setItem(EquipmentSlot slot, ItemStack item);

    int getHeldItemSlot();

    void setHeldItemSlot(int slot);

    @Override
    HumanEntity getHolder();
}
