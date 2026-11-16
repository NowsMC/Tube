package org.bukkit.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;

public interface InventoryView {

    int OUTSIDE = -999;

    Inventory getTopInventory();

    Inventory getBottomInventory();

    HumanEntity getPlayer();

    InventoryType getType();

    String getTitle();

    String getOriginalTitle();

    void setTitle(String title);

    ItemStack getCursor();

    void setCursor(ItemStack item);

    void close();

    default int countSlots() {
        return getTopInventory().getSize() + 36;
    }

    default Inventory getInventory(int rawSlot) {
        if (rawSlot == OUTSIDE || rawSlot == -1 || rawSlot < 0 || rawSlot >= countSlots()) {
            return null;
        }
        return rawSlot < getTopInventory().getSize() ? getTopInventory() : getBottomInventory();
    }

    default int convertSlot(int rawSlot) {
        int topSize = getTopInventory().getSize();
        if (rawSlot < topSize) {
            return rawSlot;
        }
        int slot = rawSlot - topSize;
        return slot >= 27 ? slot - 27 : slot + 9;
    }

    default InventoryType.SlotType getSlotType(int slot) {
        if (slot < 0) {
            return InventoryType.SlotType.OUTSIDE;
        }
        if (slot < getTopInventory().getSize()) {
            return InventoryType.SlotType.CONTAINER;
        }
        return convertSlot(slot) < 9 ? InventoryType.SlotType.QUICKBAR : InventoryType.SlotType.CONTAINER;
    }

    default ItemStack getItem(int slot) {
        Inventory inventory = getInventory(slot);
        return inventory == null ? null : inventory.getItem(convertSlot(slot));
    }

    default void setItem(int slot, ItemStack item) {
        Inventory inventory = getInventory(slot);
        if (inventory != null) {
            inventory.setItem(convertSlot(slot), item);
        } else if (item != null) {
            getPlayer().getWorld().dropItemNaturally(getPlayer().getLocation(), item);
        }
    }
}
