package space.nows.tube.bukkit.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class BukkitPlayerInventory extends BukkitInventory implements PlayerInventory {

    private static final int STORAGE = 36;
    private static final int SIZE = 41;

    private final space.nows.tube.api.Player player;
    private final HumanEntity holder;

    public BukkitPlayerInventory(space.nows.tube.api.Player player, HumanEntity holder) {
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
            case 36 -> ItemConverter.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.FEET));
            case 37 -> ItemConverter.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.LEGS));
            case 38 -> ItemConverter.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.CHEST));
            case 39 -> ItemConverter.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.HEAD));
            case 40 -> ItemConverter.fromTube(player.equipment(space.nows.tube.api.EquipmentSlot.OFF_HAND));
            default -> {
                if (slot < 0 || slot >= STORAGE) {
                    throw new IndexOutOfBoundsException("Slot " + slot);
                }
                yield ItemConverter.fromTube(tube.item(slot));
            }
        };
    }

    @Override
    void write(int slot, ItemStack item) {
        space.nows.tube.api.ItemStack converted = ItemConverter.toTube(item);
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
        return ItemConverter.nonNull(read(getHeldItemSlot()));
    }

    @Override
    public void setItemInMainHand(ItemStack item) {
        write(getHeldItemSlot(), item);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return ItemConverter.nonNull(read(40));
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
        return ItemConverter.nonNull(read(slotOf(slot)));
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
        return other instanceof BukkitPlayerInventory inventory && inventory.player.uuid().equals(player.uuid());
    }

    @Override
    public int hashCode() {
        return player.uuid().hashCode();
    }
}
