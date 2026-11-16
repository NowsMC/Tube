package space.nows.tube.api;

import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Objects;

public record ItemStack(String type, int amount, Component name, List<Component> lore) {

    public static final ItemStack AIR = new ItemStack("minecraft:air", 0, null, List.of());

    public ItemStack {
        Objects.requireNonNull(type, "type");
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        type = type.indexOf(':') < 0 ? "minecraft:" + type : type;
        lore = List.copyOf(lore);
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
        return new ItemStack(type, amount, name, lore);
    }

    public ItemStack withName(Component name) {
        return new ItemStack(type, amount, name, lore);
    }

    public ItemStack withLore(List<Component> lore) {
        return new ItemStack(type, amount, name, lore);
    }
}
