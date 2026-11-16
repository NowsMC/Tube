package org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.ItemMetas;

import java.util.Objects;

public class ItemStack implements Cloneable {

    private Material type;
    private int amount;
    private short durability;
    private ItemMeta meta;

    public ItemStack(Material type) {
        this(type, 1);
    }

    public ItemStack(Material type, int amount) {
        this(type, amount, (short) 0);
    }

    public ItemStack(Material type, int amount, short damage) {
        this.type = Objects.requireNonNull(type, "type");
        this.amount = amount;
        this.durability = damage;
    }

    public Material getType() {
        return type;
    }

    public void setType(Material type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public short getDurability() {
        return durability;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

    public int getMaxStackSize() {
        return type.getMaxStackSize();
    }

    public boolean isEmpty() {
        return type.isAir() || amount <= 0;
    }

    public boolean hasItemMeta() {
        return meta != null;
    }

    public ItemMeta getItemMeta() {
        return meta == null ? ItemMetas.create() : meta.clone();
    }

    public boolean setItemMeta(ItemMeta meta) {
        this.meta = meta == null ? null : meta.clone();
        return true;
    }

    public boolean isSimilar(ItemStack other) {
        return other != null && type == other.type && durability == other.durability && Objects.equals(meta, other.meta);
    }

    @Override
    public ItemStack clone() {
        ItemStack copy = new ItemStack(type, amount, durability);
        copy.meta = meta == null ? null : meta.clone();
        return copy;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemStack stack && amount == stack.amount && isSimilar(stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, durability, meta);
    }

    @Override
    public String toString() {
        return "ItemStack{" + type + " x " + amount + "}";
    }
}
