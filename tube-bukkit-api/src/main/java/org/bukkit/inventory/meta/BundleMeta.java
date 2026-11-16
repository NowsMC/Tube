package org.bukkit.inventory.meta;

public interface BundleMeta extends org.bukkit.inventory.meta.ItemMeta {

    boolean hasItems();

    java.util.List<org.bukkit.inventory.ItemStack> getItems();

    void setItems(java.util.List<org.bukkit.inventory.ItemStack> p0);

    void addItem(org.bukkit.inventory.ItemStack p0);
}
