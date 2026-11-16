package space.nows.tube.bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

class BukkitInventory implements Inventory {

    final space.nows.tube.api.Inventory tube;

    BukkitInventory(space.nows.tube.api.Inventory tube) {
        this.tube = tube;
    }

    int storageSize() {
        return tube.size();
    }

    ItemStack read(int slot) {
        return Items.fromTube(tube.item(slot));
    }

    void write(int slot, ItemStack item) {
        tube.setItem(slot, Items.toTube(item));
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

    static final class OfPlayer extends BukkitInventory implements PlayerInventory {

        private static final int STORAGE = 36;
        private static final int SIZE = 41;

        private final space.nows.tube.api.Player player;
        private final HumanEntity holder;

        OfPlayer(space.nows.tube.api.Player player, HumanEntity holder) {
            super(player.inventory());
            this.player = player;
            this.holder = holder;
        }

        @Override
        int storageSize() {
            return STORAGE;
        }

        @Override
        public int getSize() {
            return SIZE;
        }

        @Override
        ItemStack read(int slot) {
            return switch (slot) {
                case 36 -> Items.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.FEET));
                case 37 -> Items.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.LEGS));
                case 38 -> Items.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.CHEST));
                case 39 -> Items.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.HEAD));
                case 40 -> Items.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.OFF_HAND));
                default -> {
                    if (slot < 0 || slot >= STORAGE) {
                        throw new IndexOutOfBoundsException("Slot " + slot);
                    }
                    yield Items.fromTube(tube.item(slot));
                }
            };
        }

        @Override
        void write(int slot, ItemStack item) {
            space.nows.tube.api.ItemStack converted = Items.toTube(item);
            switch (slot) {
                case 36 -> player.setEquipment(space.nows.tube.api.EquipmentSlot.FEET, converted);
                case 37 -> player.setEquipment(space.nows.tube.api.EquipmentSlot.LEGS, converted);
                case 38 -> player.setEquipment(space.nows.tube.api.EquipmentSlot.CHEST, converted);
                case 39 -> player.setEquipment(space.nows.tube.api.EquipmentSlot.HEAD, converted);
                case 40 -> player.setEquipment(space.nows.tube.api.EquipmentSlot.OFF_HAND, converted);
                default -> {
                    if (slot < 0 || slot >= STORAGE) {
                        throw new IndexOutOfBoundsException("Slot " + slot);
                    }
                    tube.setItem(slot, converted);
                }
            }
        }

        @Override
        public org.bukkit.event.inventory.InventoryType getType() {
            return InventoryType.PLAYER;
        }

        @Override
        public ItemStack[] getArmorContents() {
            return new ItemStack[] {read(36), read(37), read(38), read(39)};
        }

        @Override
        public void setArmorContents(ItemStack[] items) {
            for (int i = 0; i < 4; i++) {
                write(36 + i, i < items.length ? items[i] : null);
            }
        }

        @Override
        public ItemStack getHelmet() {
            return read(39);
        }

        @Override
        public void setHelmet(ItemStack item) {
            write(39, item);
        }

        @Override
        public ItemStack getChestplate() {
            return read(38);
        }

        @Override
        public void setChestplate(ItemStack item) {
            write(38, item);
        }

        @Override
        public ItemStack getLeggings() {
            return read(37);
        }

        @Override
        public void setLeggings(ItemStack item) {
            write(37, item);
        }

        @Override
        public ItemStack getBoots() {
            return read(36);
        }

        @Override
        public void setBoots(ItemStack item) {
            write(36, item);
        }

        @Override
        public ItemStack getItemInMainHand() {
            return Items.nonNull(read(getHeldItemSlot()));
        }

        @Override
        public void setItemInMainHand(ItemStack item) {
            write(getHeldItemSlot(), item);
        }

        @Override
        public ItemStack getItemInOffHand() {
            return Items.nonNull(read(40));
        }

        @Override
        public void setItemInOffHand(ItemStack item) {
            write(40, item);
        }

        @Override
        public ItemStack getItemInHand() {
            return getItemInMainHand();
        }

        @Override
        public void setItemInHand(ItemStack item) {
            setItemInMainHand(item);
        }

        @Override
        public ItemStack getItem(EquipmentSlot slot) {
            return Items.nonNull(read(slotOf(slot)));
        }

        @Override
        public void setItem(EquipmentSlot slot, ItemStack item) {
            write(slotOf(slot), item);
        }

        private int slotOf(EquipmentSlot slot) {
            return switch (slot) {
                case HAND -> getHeldItemSlot();
                case OFF_HAND -> 40;
                case FEET -> 36;
                case LEGS -> 37;
                case CHEST -> 38;
                case HEAD -> 39;
                case BODY -> throw new IllegalArgumentException("BODY is not a player slot");
            };
        }

        @Override
        public int getHeldItemSlot() {
            return player.heldSlot();
        }

        @Override
        public void setHeldItemSlot(int slot) {
            if (slot < 0 || slot > 8) {
                throw new IllegalArgumentException("Slot is not between 0 and 8 inclusive");
            }
            player.setHeldSlot(slot);
        }

        @Override
        public HumanEntity getHolder() {
            return holder;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof OfPlayer inventory && inventory.player.uuid().equals(player.uuid());
        }

        @Override
        public int hashCode() {
            return player.uuid().hashCode();
        }
    }
}
