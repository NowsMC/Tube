package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.Material;
import space.nows.tube.api.ItemStack;

import java.util.List;

final class ItemMapper {

    private ItemMapper() {
    }

    static ItemStack fromMinestom(net.minestom.server.item.ItemStack stack) {
        if (stack.isAir()) {
            return ItemStack.AIR;
        }
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        List<Component> lore = stack.get(DataComponents.LORE);
        var enchantments = new java.util.HashMap<String, Integer>();
        var list = stack.get(DataComponents.ENCHANTMENTS);
        if (list != null) {
            list.enchantments().forEach((key, level) -> enchantments.put(key.key().asString(), level));
        }
        Integer damage = stack.get(DataComponents.DAMAGE);
        return new ItemStack(stack.material().key().asString(), stack.amount(), name, lore == null ? List.of() : lore, enchantments, damage == null ? 0 : damage);
    }

    static net.minestom.server.item.ItemStack toMinestom(ItemStack stack) {
        if (stack.isAir()) {
            return net.minestom.server.item.ItemStack.AIR;
        }
        Material material = Material.fromKey(stack.type());
        if (material == null) {
            throw new IllegalArgumentException("Unknown item: " + stack.type());
        }
        net.minestom.server.item.ItemStack result = net.minestom.server.item.ItemStack.of(material, stack.amount());
        if (stack.name() != null) {
            result = result.withCustomName(stack.name());
        }
        if (!stack.lore().isEmpty()) {
            result = result.with(DataComponents.LORE, stack.lore());
        }
        if (!stack.enchantments().isEmpty()) {
            var map = new java.util.HashMap<net.minestom.server.registry.RegistryKey<net.minestom.server.item.enchant.Enchantment>, Integer>();
            stack.enchantments().forEach((key, level) -> map.put(net.minestom.server.registry.RegistryKey.of(key), level));
            result = result.with(DataComponents.ENCHANTMENTS, new net.minestom.server.item.component.EnchantmentList(map));
        }
        if (stack.damage() > 0) {
            result = result.with(DataComponents.DAMAGE, stack.damage());
        }
        return result;
    }
}
