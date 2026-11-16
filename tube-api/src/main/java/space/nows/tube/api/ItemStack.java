package space.nows.tube.api;

import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ItemStack(String type, int amount, Component name, List<Component> lore, Map<String, Integer> enchantments, int damage) {

    public static final ItemStack AIR = new ItemStack("minecraft:air", 0, null, List.of());

    public ItemStack {
        Objects.requireNonNull(type, "type");
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        if (damage < 0) {
            throw new IllegalArgumentException("damage must not be negative");
        }
        type = type.indexOf(':') < 0 ? "minecraft:" + type : type;
        lore = List.copyOf(lore);
        Map<String, Integer> normalized = new HashMap<>();
        enchantments.forEach((key, level) -> normalized.put(key.indexOf(':') < 0 ? "minecraft:" + key : key, level));
        enchantments = Map.copyOf(normalized);
    }

    public ItemStack(String type, int amount, Component name, List<Component> lore) {
        this(type, amount, name, lore, Map.of(), 0);
    }

    public static ItemStack of(String type) {
        return of(type, 1);
    }

    public static ItemStack of(String type, int amount) {
        return new ItemStack(type, amount, null, List.of());
    }

    public boolean isAir() {
        return amount == 0 || type.equals("minecraft:air") || type.equals("minecraft:cave_air") || type.equals("minecraft:void_air");
    }

    public ItemStack withAmount(int amount) {
        return new ItemStack(type, amount, name, lore, enchantments, damage);
    }

    public ItemStack withName(Component name) {
        return new ItemStack(type, amount, name, lore, enchantments, damage);
    }

    public ItemStack withLore(List<Component> lore) {
        return new ItemStack(type, amount, name, lore, enchantments, damage);
    }

    public ItemStack withEnchantments(Map<String, Integer> enchantments) {
        return new ItemStack(type, amount, name, lore, enchantments, damage);
    }

    public ItemStack withDamage(int damage) {
        return new ItemStack(type, amount, name, lore, enchantments, damage);
    }
}
