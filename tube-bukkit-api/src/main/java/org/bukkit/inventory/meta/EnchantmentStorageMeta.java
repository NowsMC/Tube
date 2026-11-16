package org.bukkit.inventory.meta;

public interface EnchantmentStorageMeta extends org.bukkit.inventory.meta.ItemMeta {

    int getStoredEnchantLevel(org.bukkit.enchantments.Enchantment p0);

    java.util.Map<org.bukkit.enchantments.Enchantment, java.lang.Integer> getStoredEnchants();

    boolean addStoredEnchant(org.bukkit.enchantments.Enchantment p0, int p1, boolean p2);

    boolean removeStoredEnchant(org.bukkit.enchantments.Enchantment p0);
}
