package space.nows.tube.server.gameplay.fishing;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

final class LootEnchanting {

    private static final String RANDOM_LOOT = "#minecraft:on_random_loot";
    private static final double LOWEST_SHARE = 0.5;

    private LootEnchanting() {
    }

    static ItemStack apply(ItemStack item, RandomGenerator random) {
        DynamicRegistry<Enchantment> registry = MinecraftServer.getEnchantmentRegistry();
        RegistryTag<Enchantment> candidates = registry.getTag(TagKey.ofHash(RANDOM_LOOT));
        if (candidates == null) {
            return item;
        }
        boolean book = item.material() == Material.BOOK;
        List<RegistryKey<Enchantment>> options = new ArrayList<>();
        for (RegistryKey<Enchantment> key : candidates) {
            Enchantment enchantment = registry.get(key);
            if (enchantment != null && (book || enchantment.supportedItems().contains(item.material()))) {
                options.add(key);
            }
        }
        if (options.isEmpty()) {
            return item;
        }
        RegistryKey<Enchantment> chosen = options.get(random.nextInt(options.size()));
        int max = registry.get(chosen).maxLevel();
        int level = Math.max(1, (int) Math.round(max * (LOWEST_SHARE + random.nextDouble() * (1 - LOWEST_SHARE))));
        EnchantmentList list = new EnchantmentList(chosen, level);
        return book ? ItemStack.of(Material.ENCHANTED_BOOK).with(DataComponents.STORED_ENCHANTMENTS, list) : item.with(DataComponents.ENCHANTMENTS, list);
    }
}
