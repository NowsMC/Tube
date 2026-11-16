package space.nows.tube.bukkit.inventory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class BukkitInventory implements Inventory {

    final space.nows.tube.api.Inventory tube;

    public BukkitInventory(space.nows.tube.api.Inventory tube) {
        this.tube = tube;
    }

    int storageSize() {
        return tube.size();
    }

    ItemStack read(int slot) {
        return ItemConverter.fromTube(tube.item(slot));
    }

    void write(int slot, ItemStack item) {
        tube.setItem(slot, ItemConverter.toTube(item));
    }

    @Override
    public int getSize() {
        return tube.size();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public ItemStack getItem(int index) {
        return read(index);
    }

    @Override
    public void setItem(int index, ItemStack item) {
        write(index, item);
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack remaining = items[i].clone();
            int max = Math.max(1, Math.min(remaining.getMaxStackSize(), getMaxStackSize()));
            for (int slot = 0; slot < storageSize() && remaining.getAmount() > 0; slot++) {
                ItemStack present = read(slot);
                if (present != null && present.isSimilar(remaining) && present.getAmount() < max) {
                    int moved = Math.min(max - present.getAmount(), remaining.getAmount());
                    present.setAmount(present.getAmount() + moved);
                    write(slot, present);
                    remaining.setAmount(remaining.getAmount() - moved);
                }
            }
            for (int slot = 0; slot < storageSize() && remaining.getAmount() > 0; slot++) {
                if (read(slot) == null) {
                    int moved = Math.min(max, remaining.getAmount());
                    ItemStack placed = remaining.clone();
                    placed.setAmount(moved);
                    write(slot, placed);
                    remaining.setAmount(remaining.getAmount() - moved);
                }
            }
            if (remaining.getAmount() > 0) {
                leftover.put(i, remaining);
            }
        }
        return leftover;
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack remaining = items[i].clone();
            for (int slot = 0; slot < storageSize() && remaining.getAmount() > 0; slot++) {
                ItemStack present = read(slot);
                if (present != null && present.isSimilar(remaining)) {
                    int taken = Math.min(present.getAmount(), remaining.getAmount());
                    present.setAmount(present.getAmount() - taken);
                    write(slot, present.getAmount() > 0 ? present : null);
                    remaining.setAmount(remaining.getAmount() - taken);
                }
            }
            if (remaining.getAmount() > 0) {
                leftover.put(i, remaining);
            }
        }
        return leftover;
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[getSize()];
        for (int i = 0; i < contents.length; i++) {
            contents[i] = read(i);
        }
        return contents;
    }

    @Override
    public void setContents(ItemStack[] items) {
        if (items.length > getSize()) {
            throw new IllegalArgumentException("Invalid inventory size " + items.length + "; expected " + getSize() + " or less");
        }
        for (int i = 0; i < getSize(); i++) {
            write(i, i < items.length ? items[i] : null);
        }
    }

    @Override
    public ItemStack[] getStorageContents() {
        return Arrays.copyOf(getContents(), storageSize());
    }

    @Override
    public void setStorageContents(ItemStack[] items) {
        if (items.length > storageSize()) {
            throw new IllegalArgumentException("Invalid inventory size " + items.length + "; expected " + storageSize() + " or less");
        }
        for (int i = 0; i < storageSize(); i++) {
            write(i, i < items.length ? items[i] : null);
        }
    }

    @Override
    public boolean contains(Material material) {
        return first(material) >= 0;
    }

    @Override
    public boolean contains(Material material, int amount) {
        if (amount <= 0) {
            return true;
        }
        int total = 0;
        for (ItemStack item : all(material).values()) {
            total += item.getAmount();
            if (total >= amount) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        if (item == null) {
            return false;
        }
        int total = 0;
        for (ItemStack present : getContents()) {
            if (present != null && present.isSimilar(item)) {
                total += present.getAmount();
            }
        }
        return total >= amount;
    }

    @Override
    public HashMap<Integer, ItemStack> all(Material material) {
        HashMap<Integer, ItemStack> found = new HashMap<>();
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = read(i);
            if (item != null && item.getType() == material) {
                found.put(i, item);
            }
        }
        return found;
    }

    @Override
    public int first(Material material) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = read(i);
            if (item != null && item.getType() == material) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int firstEmpty() {
        for (int i = 0; i < storageSize(); i++) {
            if (read(i) == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void remove(Material material) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = read(i);
            if (item != null && item.getType() == material) {
                write(i, null);
            }
        }
    }

    @Override
    public void clear(int index) {
        write(index, null);
    }

    @Override
    public void clear() {
        for (int i = 0; i < getSize(); i++) {
            write(i, null);
        }
    }

    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

    @Override
    public InventoryHolder getHolder() {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        List<ItemStack> snapshot = new ArrayList<>(Arrays.asList(getContents()));
        return snapshot.listIterator();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BukkitInventory inventory && inventory.tube.equals(tube);
    }

    @Override
    public int hashCode() {
        return tube.hashCode();
    }
}
