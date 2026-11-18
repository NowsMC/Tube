package space.nows.tube.server.loot.function;

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
import space.nows.tube.server.loot.LootContext;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

record EnchantWithLevels(String options) implements LootFunction {

    private static final double LOWEST_SHARE = 0.5;

    @Override
    public ItemStack apply(ItemStack item, LootContext context) {
        DynamicRegistry<Enchantment> registry = MinecraftServer.getEnchantmentRegistry();
        RegistryTag<Enchantment> candidates = registry.getTag(TagKey.ofHash(options));
        if (candidates == null) {
            return item;
        }
        boolean book = item.material() == Material.BOOK;
        List<RegistryKey<Enchantment>> fitting = new ArrayList<>();
        for (RegistryKey<Enchantment> key : candidates) {
            Enchantment enchantment = registry.get(key);
            if (enchantment != null && (book || enchantment.supportedItems().contains(item.material()))) {
                fitting.add(key);
            }
        }
        if (fitting.isEmpty()) {
            return item;
        }
        RandomGenerator random = context.random();
        RegistryKey<Enchantment> chosen = fitting.get(random.nextInt(fitting.size()));
        int max = registry.get(chosen).maxLevel();
        int level = Math.max(1, (int) Math.round(max * (LOWEST_SHARE + random.nextDouble() * (1 - LOWEST_SHARE))));
        EnchantmentList list = new EnchantmentList(chosen, level);
        return book ? ItemStack.of(Material.ENCHANTED_BOOK).with(DataComponents.STORED_ENCHANTMENTS, list) : item.with(DataComponents.ENCHANTMENTS, list);
    }
}
