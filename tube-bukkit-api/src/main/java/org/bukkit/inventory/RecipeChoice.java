package org.bukkit.inventory;

public interface RecipeChoice extends java.util.function.Predicate<org.bukkit.inventory.ItemStack>, java.lang.Cloneable {

    org.bukkit.inventory.ItemStack getItemStack();

    boolean test(org.bukkit.inventory.ItemStack p0);
}
