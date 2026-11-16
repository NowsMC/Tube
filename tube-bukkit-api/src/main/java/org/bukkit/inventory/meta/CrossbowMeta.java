package org.bukkit.inventory.meta;

public interface CrossbowMeta extends org.bukkit.inventory.meta.ItemMeta {

    boolean hasChargedProjectiles();

    java.util.List<org.bukkit.inventory.ItemStack> getChargedProjectiles();

    void setChargedProjectiles(java.util.List<org.bukkit.inventory.ItemStack> p0);

    void addChargedProjectile(org.bukkit.inventory.ItemStack p0);
}
