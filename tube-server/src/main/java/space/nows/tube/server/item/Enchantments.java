package space.nows.tube.server.item;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;

public final class Enchantments {

    private Enchantments() {
    }

    public static int level(ItemStack item, String enchantment) {
        if (item == null || enchantment.startsWith("#")) {
            return 0;
        }
        EnchantmentList enchantments = item.get(DataComponents.ENCHANTMENTS);
        return enchantments == null ? 0 : enchantments.level(RegistryKey.<Enchantment>of(enchantment));
    }
}
