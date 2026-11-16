package space.nows.tube.bukkit.inventory;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ItemConverter {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private ItemConverter() {
    }

    static space.nows.tube.api.inventory.ItemStack toTube(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return space.nows.tube.api.inventory.ItemStack.AIR;
        }
        space.nows.tube.api.inventory.ItemStack result = space.nows.tube.api.inventory.ItemStack.of(item.getType().getKey().toString(), item.getAmount());
        if (!item.getEnchantments().isEmpty()) {
            java.util.Map<String, Integer> enchantments = new java.util.HashMap<>();
            item.getEnchantments().forEach((enchantment, level) -> enchantments.put(enchantment.getKey().toString(), level));
            result = result.withEnchantments(enchantments);
        }
        if (item.getDurability() > 0) {
            result = result.withDamage(item.getDurability());
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                result = result.withName(LEGACY.deserialize(meta.getDisplayName()));
            }
            if (meta.hasLore()) {
                result = result.withLore(meta.getLore().stream().<net.kyori.adventure.text.Component>map(LEGACY::deserialize).toList());
            }
        }
        return result;
    }

    public static ItemStack fromTube(space.nows.tube.api.inventory.ItemStack item) {
        if (item == null || item.isAir()) {
            return null;
        }
        Material material = Material.matchMaterial(item.type());
        ItemStack result = new ItemStack(material == null ? Material.AIR : material, item.amount());
        item.enchantments().forEach((key, level) -> {
            org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(key.replaceFirst("^minecraft:", "")));
            if (enchantment != null) {
                result.addUnsafeEnchantment(enchantment, level);
            }
        });
        result.setDurability((short) Math.min(Short.MAX_VALUE, item.damage()));
        if (item.name() != null || !item.lore().isEmpty()) {
            ItemMeta meta = result.getItemMeta();
            if (item.name() != null) {
                meta.setDisplayName(LEGACY.serialize(item.name()));
            }
            if (!item.lore().isEmpty()) {
                meta.setLore(item.lore().stream().map(LEGACY::serialize).toList());
            }
            result.setItemMeta(meta);
        }
        return result;
    }

    static ItemStack nonNull(ItemStack item) {
        return item == null ? new ItemStack(Material.AIR, 0) : item;
    }

    static List<ItemStack> list(ItemStack[] items) {
        return java.util.Arrays.asList(items);
    }
}
