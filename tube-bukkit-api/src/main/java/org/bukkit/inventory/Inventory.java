package org.bukkit.inventory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

import java.util.HashMap;
import java.util.ListIterator;

public interface Inventory extends Iterable<ItemStack> {

    int getSize();

    int getMaxStackSize();

    ItemStack getItem(int index);

    void setItem(int index, ItemStack item);

    HashMap<Integer, ItemStack> addItem(ItemStack... items);

    HashMap<Integer, ItemStack> removeItem(ItemStack... items);

    ItemStack[] getContents();

    void setContents(ItemStack[] items);

    ItemStack[] getStorageContents();

    void setStorageContents(ItemStack[] items);

    boolean contains(Material material);

    boolean contains(Material material, int amount);

    boolean containsAtLeast(ItemStack item, int amount);

    HashMap<Integer, ? extends ItemStack> all(Material material);

    int first(Material material);

    int firstEmpty();

    void remove(Material material);

    void clear(int index);

    void clear();

    InventoryType getType();

    InventoryHolder getHolder();

    Location getLocation();

    @Override
    ListIterator<ItemStack> iterator();
}
